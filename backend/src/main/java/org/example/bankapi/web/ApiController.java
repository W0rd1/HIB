package org.example.bankapi.web;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.example.bankapi.model.Customer;
import org.example.bankapi.model.PaymentRequest;
import org.example.bankapi.service.BankService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.config.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api")
@Validated
public class ApiController {
    private final BankService bank;
    @Value("${bank.admin.username:admin}") private String adminUsername;
    @Value("${bank.admin.password:admin123}") private String adminPassword;
    public ApiController(BankService bank){this.bank=bank;}
    public record LoginInput(@NotBlank String username,@NotBlank String password){}
    public record MoneyInput(@NotNull @DecimalMin(value="0.01") @Digits(integer=17,fraction=2) BigDecimal amount){}
    public record CreateRequest(@NotBlank String recipientUsername,@Size(max=500) String description,@NotNull @DecimalMin("0.01") @Digits(integer=17,fraction=2) BigDecimal amount,boolean editable){}
    public record Decision(@NotNull Boolean approve){}
    public record Toggle(@NotNull Boolean frozen){}
    public record ActiveToggle(@NotNull Boolean active){}
    public record Charge(@NotNull @DecimalMin("0.01") @Digits(integer=17,fraction=2) BigDecimal amount,UUID customerId){}

    @PostMapping("/auth/register") public Map<String,Object> register(@RequestBody Map<String,String> input,HttpSession session){Customer c=bank.register(input);session.setAttribute("customerId",c.getId());session.removeAttribute("admin");return customerView(c);}
    @PostMapping("/auth/login") public Map<String,Object> login(@Valid @RequestBody LoginInput input,HttpSession session){Customer c=bank.login(input.username(),input.password());session.setAttribute("customerId",c.getId());session.removeAttribute("admin");return customerView(c);}
    @PostMapping("/auth/logout") public Map<String,String> logout(HttpSession session){session.invalidate();return Map.of("message","Logged out.");}
    @GetMapping("/accounts/me") public Map<String,Object> me(HttpSession s){return customerView(current(s));}
    @PostMapping("/accounts/deposit") public Map<String,Object> deposit(@Valid @RequestBody MoneyInput i,HttpSession s){Customer c=current(s);return Map.of("balance",bank.changeBalance(c,i.amount(),true));}
    @PostMapping("/accounts/withdraw") public Map<String,Object> withdraw(@Valid @RequestBody MoneyInput i,HttpSession s){Customer c=current(s);return Map.of("balance",bank.changeBalance(c,i.amount(),false));}
    @GetMapping("/requests") public List<Map<String,Object>> requests(HttpSession s){return bank.listRequests(current(s));}
    @PostMapping("/requests") public Map<String,Object> create(@Valid @RequestBody CreateRequest i,HttpSession s){return requestView(bank.createRequest(current(s),i.recipientUsername(),i.description(),i.amount(),i.editable()));}
    @PostMapping("/requests/{id}/decision") public Map<String,Object> decide(@PathVariable UUID id,@Valid @RequestBody Decision i,HttpSession s){return requestView(bank.decide(current(s),id,i.approve()));}
    @PostMapping("/admin/login") public Map<String,String> adminLogin(@Valid @RequestBody LoginInput i,HttpSession s){if(!adminUsername.equals(i.username())||!adminPassword.equals(i.password()))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid admin credentials.");s.setAttribute("admin",true);s.removeAttribute("customerId");return Map.of("message","Admin signed in.");}
    @GetMapping("/admin/customers") public List<Map<String,Object>> customers(HttpSession s){requireAdmin(s);return bank.allCustomers().stream().map(this::customerView).toList();}
    @PatchMapping("/admin/customers/{username}/frozen") public Map<String,Object> frozen(@PathVariable String username,@Valid @RequestBody Toggle i,HttpSession s){requireAdmin(s);return customerView(bank.setFrozen(username,i.frozen()));}
    @PatchMapping("/admin/customers/{username}/active") public Map<String,Object> active(@PathVariable String username,@Valid @RequestBody ActiveToggle i,HttpSession s){requireAdmin(s);return customerView(bank.setActive(username,i.active()));}
    @PostMapping("/admin/customers/{username}/charge") public Map<String,Object> charge(@PathVariable String username,@Valid @RequestBody Charge i,HttpSession s){requireAdmin(s);return Map.of("balance",bank.adminCharge(username,i.customerId(),i.amount()));}
    private Customer current(HttpSession s){Object id=s.getAttribute("customerId");if(!(id instanceof UUID))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Please sign in.");return bank.byId((UUID)id);}
    private void requireAdmin(HttpSession s){if(!Boolean.TRUE.equals(s.getAttribute("admin")))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Admin sign in required.");}
    private Map<String,Object> customerView(Customer c){Map<String,Object> v=new LinkedHashMap<>();v.put("id",c.getId());v.put("firstName",c.getFirstName());v.put("lastName",c.getLastName());v.put("email",c.getEmail());v.put("phoneNumber",c.getPhoneNumber());v.put("address",c.getAddress());v.put("country",c.getCountry());v.put("username",c.getUsername());v.put("balance",c.getBalance());v.put("frozen",c.isFrozen());v.put("active",c.isActive());return v;}
    private Map<String,Object> requestView(PaymentRequest r){Map<String,Object> v=new LinkedHashMap<>();v.put("id",r.getId());v.put("requesterUsername",r.getRequester().getUsername());v.put("payerUsername",r.getPayer().getUsername());v.put("amount",r.getAmount());v.put("description",r.getDescription());v.put("editable",r.isEditable());v.put("status",r.getStatus());v.put("createdAt",r.getCreatedAt());v.put("expiresAt",r.getExpiresAt());return v;}
}

@Configuration
class CorsConfiguration implements WebMvcConfigurer {
    @Override public void addCorsMappings(CorsRegistry registry){registry.addMapping("/api/**").allowedOrigins("http://localhost:4200").allowedMethods("GET","POST","PATCH","OPTIONS").allowedHeaders("*").allowCredentials(true).maxAge(3600);}
}
