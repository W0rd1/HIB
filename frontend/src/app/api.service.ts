import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type Payload = Record<string, unknown>;

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly baseUrl = (globalThis as typeof globalThis & { HIB_API_BASE_URL?: string }).HIB_API_BASE_URL || 'http://localhost:8080';
  constructor(private readonly http: HttpClient) {}
  register(body: Payload): Observable<unknown> { return this.http.post(`${this.baseUrl}/api/auth/register`, body, { withCredentials: true }); }
  login(body: Payload): Observable<unknown> { return this.http.post(`${this.baseUrl}/api/auth/login`, body, { withCredentials: true }); }
  logout(): Observable<unknown> { return this.http.post(`${this.baseUrl}/api/auth/logout`, {}, { withCredentials: true }); }
  me(): Observable<unknown> { return this.http.get(`${this.baseUrl}/api/accounts/me`, { withCredentials: true }); }
  deposit(amount: number): Observable<unknown> { return this.http.post(`${this.baseUrl}/api/accounts/deposit`, { amount }, { withCredentials: true }); }
  withdraw(amount: number): Observable<unknown> { return this.http.post(`${this.baseUrl}/api/accounts/withdraw`, { amount }, { withCredentials: true }); }
  requests(): Observable<unknown> { return this.http.get(`${this.baseUrl}/api/requests`, { withCredentials: true }); }
  createRequest(body: Payload): Observable<unknown> { return this.http.post(`${this.baseUrl}/api/requests`, body, { withCredentials: true }); }
  decideRequest(id: string, approve: boolean): Observable<unknown> { return this.http.post(`${this.baseUrl}/api/requests/${encodeURIComponent(id)}/decision`, { approve }, { withCredentials: true }); }
  adminLogin(body: Payload): Observable<unknown> { return this.http.post(`${this.baseUrl}/api/admin/login`, body, { withCredentials: true }); }
  customers(): Observable<unknown> { return this.http.get(`${this.baseUrl}/api/admin/customers`, { withCredentials: true }); }
  setFrozen(username: string, frozen: boolean): Observable<unknown> { return this.http.patch(`${this.baseUrl}/api/admin/customers/${encodeURIComponent(username)}/frozen`, { frozen }, { withCredentials: true }); }
  setActive(username: string, active: boolean): Observable<unknown> { return this.http.patch(`${this.baseUrl}/api/admin/customers/${encodeURIComponent(username)}/active`, { active }, { withCredentials: true }); }
  charge(username: string, customerId: string, amount: number): Observable<unknown> { return this.http.post(`${this.baseUrl}/api/admin/customers/${encodeURIComponent(username)}/charge`, { customerId, amount }, { withCredentials: true }); }
}
