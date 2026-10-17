package org.example.bankapi.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "customers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_customer_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_customer_email", columnNames = "email")
})
public class Customer {
    @Id @GeneratedValue private UUID id;
    @Version private long version;
    @Column(nullable = false) private String firstName;
    @Column(nullable = false) private String lastName;
    @Column(nullable = false) private String email;
    private String phoneNumber;
    private String address;
    private String country;
    @Column(nullable = false) private String username;
    @Column(nullable = false) private String passwordHash;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal balance = BigDecimal.ZERO;
    @Column(nullable = false) private boolean frozen = false;
    @Column(nullable = false) private boolean active = true;

    protected Customer() {}
    public Customer(String firstName, String lastName, String email, String phoneNumber, String address, String country, String username, String passwordHash) {
        this.firstName=firstName; this.lastName=lastName; this.email=email; this.phoneNumber=phoneNumber;
        this.address=address; this.country=country; this.username=username; this.passwordHash=passwordHash;
    }
    public UUID getId(){return id;} public String getFirstName(){return firstName;} public String getLastName(){return lastName;}
    public String getEmail(){return email;} public String getPhoneNumber(){return phoneNumber;} public String getAddress(){return address;}
    public String getCountry(){return country;} public String getUsername(){return username;} public String getPasswordHash(){return passwordHash;}
    public BigDecimal getBalance(){return balance;} public void setBalance(BigDecimal balance){this.balance=balance;}
    public boolean isFrozen(){return frozen;} public void setFrozen(boolean frozen){this.frozen=frozen;}
    public boolean isActive(){return active;} public void setActive(boolean active){this.active=active;}
}
