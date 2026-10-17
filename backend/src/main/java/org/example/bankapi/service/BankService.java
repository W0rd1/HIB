package org.example.bankapi.service;

import org.example.bankapi.model.Customer;
import org.example.bankapi.model.PaymentRequest;
import org.example.bankapi.repository.CustomerRepository;
import org.example.bankapi.repository.PaymentRequestRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.math.BigDecimal;
import java.util.*;

@Service
public class BankService {
    private final CustomerRepository customers;
    private final PaymentRequestRepository requests;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    public BankService(CustomerRepository customers, PaymentRequestRepository requests) { this.customers=customers; this.requests=requests; }

    @Transactional
    public Customer register(Map<String,String> input) {
        String username=normalizeUsername(required(input,"username")); String email=required(input,"email").trim().toLowerCase(Locale.ROOT);
        if(customers.existsByUsernameIgnoreCase(username) || customers.existsByEmailIgnoreCase(email)) throw new ResponseStatusException(HttpStatus.CONFLICT,"Username or email is already registered.");
        try { return customers.saveAndFlush(new Customer(required(input,"firstName").trim(), required(input,"lastName").trim(), email,
                input.getOrDefault("phoneNumber",""), input.getOrDefault("address",""), input.getOrDefault("country",""), username,
                encoder.encode(required(input,"password")))); }
        catch(DataIntegrityViolationException ex){throw new ResponseStatusException(HttpStatus.CONFLICT,"Username or email is already registered.");}
    }
    public Customer login(String username,String password){ Customer c=customers.findByUsernameIgnoreCase(normalizeUsername(username)).orElseThrow(()->unauthorized("Invalid username or password."));
        if(!c.isActive() || !encoder.matches(password,c.getPasswordHash())) throw unauthorized("Invalid username or password."); return c; }
    @Transactional public BigDecimal changeBalance(Customer c, BigDecimal amount, boolean deposit){validateAmount(amount); c=refresh(c);
        if(!c.isActive()) throw forbidden("Account is inactive."); if(c.isFrozen()) throw forbidden("Account is frozen.");
        if(!deposit && c.getBalance().compareTo(amount)<0) throw bad("Insufficient funds.");
        c.setBalance(deposit?c.getBalance().add(amount):c.getBalance().subtract(amount)); return c.getBalance(); }
    @Transactional public PaymentRequest createRequest(Customer requester,String payerName,String description,BigDecimal amount,boolean editable){
        validateAmount(amount); requester=refresh(requester); if(!requester.isActive())throw forbidden("Account is inactive.");
        Customer payer=customers.findByUsernameIgnoreCase(normalizeUsername(payerName)).orElseThrow(()->bad("Customer not found."));
        if(payer.getId().equals(requester.getId()))throw bad("You cannot request money from yourself.");
        if(!payer.isActive())throw forbidden("The requested account is inactive.");
        return requests.save(new PaymentRequest(requester,payer,amount,description,editable)); }
    @Transactional public PaymentRequest decide(Customer actor,UUID id,boolean approve){ PaymentRequest r=requests.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Request not found."));
        if(!r.getPayer().getId().equals(actor.getId()))throw forbidden("This request is not in your inbox.");
        if(r.getStatus()!=PaymentRequest.Status.PENDING)throw bad("This request is no longer pending.");
        if(!r.getExpiresAt().isAfter(java.time.Instant.now())){r.setStatus(PaymentRequest.Status.EXPIRED);return r;}
        if(approve){Customer payer=refresh(r.getPayer()), requester=refresh(r.getRequester());
            if(!payer.isActive()||!requester.isActive())throw forbidden("Both accounts must be active to transfer money.");
            if(payer.isFrozen()||requester.isFrozen())throw forbidden("Both accounts must be unfrozen to transfer money.");
            if(payer.getBalance().compareTo(r.getAmount())<0)throw bad("Insufficient funds.");
            payer.setBalance(payer.getBalance().subtract(r.getAmount())); requester.setBalance(requester.getBalance().add(r.getAmount())); r.setStatus(PaymentRequest.Status.PAID);
        } else r.setStatus(PaymentRequest.Status.REJECTED); return r; }
    @Transactional public List<Map<String,Object>> listRequests(Customer customer){List<PaymentRequest> all=requests.findForCustomer(customer.getId());
        var out=new ArrayList<Map<String,Object>>(); for(PaymentRequest r:all){if(r.getStatus()==PaymentRequest.Status.PENDING&&!r.getExpiresAt().isAfter(java.time.Instant.now()))r.setStatus(PaymentRequest.Status.EXPIRED);
            Map<String,Object> row=new LinkedHashMap<>(); row.put("id",r.getId());row.put("requesterUsername",r.getRequester().getUsername());row.put("payerUsername",r.getPayer().getUsername());row.put("amount",r.getAmount());row.put("description",r.getDescription());row.put("editable",r.isEditable());row.put("status",r.getStatus());row.put("createdAt",r.getCreatedAt());row.put("expiresAt",r.getExpiresAt());row.put("inbox",r.getPayer().getId().equals(customer.getId()));row.put("notification",r.getRequester().getId().equals(customer.getId())&&r.getStatus()==PaymentRequest.Status.REJECTED?"Your request from "+r.getPayer().getUsername()+" has been rejected.":null);out.add(row);}
        return out; }
    @Transactional public Customer setFrozen(String username,boolean frozen){Customer c=byUsername(username);c.setFrozen(frozen);return c;}
    @Transactional public Customer setActive(String username,boolean active){Customer c=byUsername(username);c.setActive(active);return c;}
    @Transactional public BigDecimal adminCharge(String username,UUID customerId,BigDecimal amount){validateAmount(amount);Customer c=byUsername(username);if(customerId!=null&&!customerId.equals(c.getId()))throw bad("Customer ID does not match username.");c.setBalance(c.getBalance().add(amount));return c.getBalance();}
    public List<Customer> allCustomers(){return customers.findAll();}
    @Transactional
    @Scheduled(fixedDelay = 60000)
    public void expireRequests(){for(PaymentRequest r:requests.findByStatusAndExpiresAtBefore(PaymentRequest.Status.PENDING,java.time.Instant.now()))r.setStatus(PaymentRequest.Status.EXPIRED);}
    public Customer byUsername(String username){return customers.findByUsernameIgnoreCase(normalizeUsername(username)).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Customer not found."));}
    public Customer byId(UUID id){return customers.findById(id).orElseThrow(()->unauthorized("Account no longer exists."));}
    public Customer refresh(Customer c){return customers.findById(c.getId()).orElseThrow(()->unauthorized("Account no longer exists."));}
    private static String required(Map<String,String> map,String key){String v=map.get(key);if(v==null||v.isBlank())throw bad(key+" is required.");return v;}
    private static String normalizeUsername(String username){return username==null?"":username.trim().toLowerCase(Locale.ROOT);}
    private static void validateAmount(BigDecimal a){if(a==null||a.compareTo(BigDecimal.ZERO)<=0||a.scale()>2)throw bad("Amount must be positive and have at most two decimal places.");}
    private static ResponseStatusException bad(String m){return new ResponseStatusException(HttpStatus.BAD_REQUEST,m);}
    private static ResponseStatusException forbidden(String m){return new ResponseStatusException(HttpStatus.FORBIDDEN,m);}
    private static ResponseStatusException unauthorized(String m){return new ResponseStatusException(HttpStatus.UNAUTHORIZED,m);}
}
