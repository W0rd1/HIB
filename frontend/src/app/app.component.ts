import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Observable } from 'rxjs';
import { ApiService, Payload } from './api.service';

type RecordValue = Record<string, any>;
type RequestItem = RecordValue;
type Customer = RecordValue;

@Component({ selector: 'hib-root', standalone: true, imports: [CommonModule, FormsModule, ReactiveFormsModule], templateUrl: './app.component.html', styleUrl: './app.component.css' })
export class AppComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(ApiService);
  mode: 'guest' | 'customer' | 'admin' = 'guest';
  registerMode = false;
  adminMode = false;
  busy = false;
  message = '';
  messageKind: 'success' | 'error' = 'success';
  account: RecordValue = {};
  requests: RequestItem[] = [];
  customers: Customer[] = [];
  chargeAmounts: Record<string, number | null> = {};
  transactionOpen = false;
  transactionKind: 'deposit' | 'withdraw' = 'deposit';
  readonly year = new Date().getFullYear();
  readonly loginForm = this.fb.nonNullable.group({ username: ['', Validators.required], password: ['', Validators.required] });
  readonly adminForm = this.fb.nonNullable.group({ username: ['', Validators.required], password: ['', Validators.required] });
  readonly registerForm = this.fb.nonNullable.group({
    firstName: ['', Validators.required], lastName: ['', Validators.required], email: ['', [Validators.required, Validators.email]],
    phoneNumber: ['', Validators.required], address: ['', Validators.required], country: ['', Validators.required],
    username: ['', Validators.required], password: ['', [Validators.required, Validators.minLength(6)]]
  });
  readonly amountForm = this.fb.nonNullable.group({ amount: [null as number | null, [Validators.required, Validators.min(0.01), Validators.pattern(/^\d+(?:\.\d{1,2})?$/)]] });
  readonly requestForm = this.fb.nonNullable.group({ recipientUsername: ['', Validators.required], description: ['', Validators.required], amount: [null as number | null, [Validators.required, Validators.min(0.01), Validators.pattern(/^\d+(?:\.\d{1,2})?$/)]], editable: [false] });

  ngOnInit(): void {
    this.api.me().subscribe({ next: value => { this.account = this.asRecord(value); this.mode = 'customer'; this.refreshRequests(); }, error: () => this.mode = 'guest' });
  }
  login(): void { if (this.loginForm.invalid) return; this.run(this.api.login(this.loginForm.getRawValue()), 'Signed in successfully.', () => this.loadAccount()); }
  register(): void {
    if (this.registerForm.invalid) return;
    this.run(this.api.register(this.registerForm.getRawValue()), 'Account created. You can now sign in.', () => {
      this.registerMode = false; this.loginForm.patchValue({ username: this.registerForm.controls.username.value }); this.registerForm.reset();
    });
  }
  adminLogin(): void { if (this.adminForm.invalid) return; this.run(this.api.adminLogin(this.adminForm.getRawValue()), 'Administrator signed in.', () => { this.mode = 'admin'; this.loadCustomers(); }); }
  toggleAdmin(): void { this.adminMode = !this.adminMode; this.message = ''; }
  logout(): void { this.api.logout().subscribe({ next: () => this.resetSession(), error: () => this.resetSession() }); }
  private resetSession(): void { this.mode = 'guest'; this.adminMode = false; this.account = {}; this.requests = []; this.customers = []; this.message = ''; }
  private loadAccount(): void { this.api.me().subscribe({ next: value => { this.account = this.asRecord(value); this.mode = 'customer'; this.message = ''; this.refreshRequests(); }, error: error => this.showError(error) }); }
  refreshRequests(): void { this.api.requests().subscribe({ next: value => this.requests = this.asArray(value, ['requests', 'items', 'inbox']), error: error => this.showError(error) }); }
  loadCustomers(): void { this.api.customers().subscribe({ next: value => this.customers = this.asArray(value, ['customers', 'items']), error: error => this.showError(error) }); }
  submitTransaction(): void {
    const amount = Number(this.amountForm.controls.amount.value); if (!amount || amount <= 0) return;
    const call = this.transactionKind === 'deposit' ? this.api.deposit(amount) : this.api.withdraw(amount);
    this.run(call, this.transactionKind === 'deposit' ? 'Deposit completed.' : 'Withdrawal completed.', () => {
      this.transactionOpen = false; this.amountForm.reset();
      this.api.me().subscribe({ next: value => this.account = this.asRecord(value), error: error => this.showError(error) });
    });
  }
  createRequest(): void {
    if (this.requestForm.invalid) return;
    const v = this.requestForm.getRawValue();
    const body: Payload = { recipientUsername: v.recipientUsername.trim(), description: v.description.trim(), amount: Number(v.amount), editable: v.editable };
    this.run(this.api.createRequest(body), 'Payment request sent.', () => { this.requestForm.reset({ recipientUsername: '', description: '', amount: null, editable: false }); this.refreshRequests(); });
  }
  decide(item: RequestItem, approve: boolean): void {
    const id = String(item.uuid ?? item.id ?? '');
    if (!id) { this.showMessage('This request is missing its ID and cannot be updated.', 'error'); return; }
    this.run(this.api.decideRequest(id, approve), approve ? 'Request approved and payment processed.' : 'Request rejected.', () => {
      this.refreshRequests(); this.api.me().subscribe({ next: value => this.account = this.asRecord(value), error: () => undefined });
    });
  }
  toggleFrozen(customer: Customer): void { const username = this.username(customer); this.run(this.api.setFrozen(username, !customer.frozen), `Account ${customer.frozen ? 'unfrozen' : 'frozen'}.`, () => this.loadCustomers()); }
  toggleActive(customer: Customer): void { const username = this.username(customer); const active = this.accountActive(customer); this.run(this.api.setActive(username, !active), `Account ${active ? 'deactivated' : 'activated'}.`, () => this.loadCustomers()); }
  charge(customer: Customer): void {
    const username = this.username(customer); const amount = Number(this.chargeAmounts[username]); const customerId = String(customer.customerId ?? customer.id ?? '');
    if (!amount || amount <= 0 || !customerId) { this.showMessage('Enter a valid amount; this customer record must include an ID.', 'error'); return; }
    this.run(this.api.charge(username, customerId, amount), 'Account charge applied.', () => { this.chargeAmounts[username] = null; this.loadCustomers(); });
  }
  private run(call: Observable<unknown>, success: string, after?: () => void): void {
    this.busy = true; this.message = '';
    call.subscribe({ next: () => { this.busy = false; this.showMessage(success, 'success'); after?.(); }, error: error => { this.busy = false; this.showError(error); } });
  }
  private showError(error: unknown): void {
    const e = error as { error?: unknown; message?: string }; const body = e?.error;
    const detail = typeof body === 'string' ? body : body && typeof body === 'object' ? String((body as RecordValue).message ?? (body as RecordValue).error ?? '') : '';
    this.showMessage(detail || e?.message || 'Something went wrong. Please try again.', 'error');
  }
  private showMessage(text: string, kind: 'success' | 'error'): void { this.message = text; this.messageKind = kind; }
  private asRecord(value: unknown): RecordValue {
    const item = value && typeof value === 'object' ? value as RecordValue : {};
    return item.account && typeof item.account === 'object' ? item.account : item.customer && typeof item.customer === 'object' ? item.customer : item;
  }
  private asArray(value: unknown, keys: string[]): any[] {
    if (Array.isArray(value)) return value;
    if (value && typeof value === 'object') { const object = value as RecordValue; for (const key of keys) if (Array.isArray(object[key])) return object[key]; }
    return [];
  }
  balance(item: RecordValue): number { return Number(item.balance ?? item.accountBalance ?? item.availableBalance ?? 0); }
  username(item: RecordValue): string { return String(item.username ?? item.userName ?? '—'); }
  fullName(item: RecordValue): string { return [item.firstName, item.lastName].filter(Boolean).join(' ') || this.username(item); }
  initials(item: RecordValue): string { return this.fullName(item).split(/[\s@._-]+/).filter(Boolean).slice(0, 2).map((part: string) => part[0]?.toUpperCase()).join('') || 'H'; }
  accountActive(item: RecordValue): boolean { return item.active !== false && item.isActive !== false; }
  money(amount: unknown): string { const value = Number(amount); return new Intl.NumberFormat('en-IE', { style: 'currency', currency: 'EUR' }).format(Number.isFinite(value) ? value : 0); }
  otherParty(item: RequestItem): string { return String(item.inbox === true ? item.requesterUsername ?? 'HIB customer' : item.payerUsername ?? 'HIB customer'); }
  requestTitle(item: RequestItem): string { return item.inbox === true ? `${this.otherParty(item)} requested money` : `Request to ${this.otherParty(item)}`; }
  notificationText(item: RequestItem): string { return typeof item.notification === 'string' ? item.notification : ''; }
  requestInitial(item: RequestItem): string { return this.initials({ firstName: this.otherParty(item) }); }
  isPending(item: RequestItem): boolean { return String(item.status ?? 'PENDING').toUpperCase() === 'PENDING'; }
  canDecide(item: RequestItem): boolean { return item.inbox === true && this.isPending(item); }
  prettyStatus(status: unknown): string { const value = String(status ?? 'PENDING').toLowerCase(); return value.charAt(0).toUpperCase() + value.slice(1); }
  statusClass(status: unknown): string { return `status-${String(status ?? 'pending').toLowerCase()}`; }
  dateLabel(item: RequestItem): string { const value = item.createdAt ?? item.createdDate ?? item.timestamp; if (!value) return 'Just now'; const date = new Date(value); return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' }); }
  trackRequest(index: number, item: RequestItem): string { return String(item.uuid ?? item.id ?? index); }
  trackCustomer(index: number, customer: Customer): string { return this.username(customer) || String(index); }
  countActive(): number { return this.customers.filter(item => this.accountActive(item)).length; }
  countFrozen(): number { return this.customers.filter(item => item.frozen).length; }
}
