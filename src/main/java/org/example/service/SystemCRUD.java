package org.example.service;

import org.example.model.Customer;
import org.example.repository.Data;

import java.math.BigDecimal;
import java.util.UUID;

import java.util.stream.*;

public class SystemCRUD {
    private final Data customers = new Data();

    // This method will create a new customer
    // fn, ln, email, phone, address, country
    public Customer register(Data data, String fn, String ln, String email, String phone, String address,
                            String country, String username, String password){
        String normalizedUsername = username.trim().toLowerCase();
        String normalizedEmail = email.trim().toLowerCase();
        if (normalizedUsername.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (normalizedEmail.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }
        if (data.findCustomer(normalizedUsername) != null) {
            throw new IllegalArgumentException("That username is already registered. Please choose another.");
        }
        if (data.findCustomerByEmail(normalizedEmail) != null) {
            throw new IllegalArgumentException("That email address is already registered.");
        }

        UUID CustomerID = UUID.randomUUID();
        BigDecimal balance = BigDecimal.ZERO;
        Customer customer = new Customer(
                CustomerID,
                fn,
                ln,
                normalizedEmail,
                phone,
                address,
                country,
                normalizedUsername,
                password,
                balance,
                true,
                false);
        data.addCustomer(customer);
        return customer;
    }

    // This method will add the customer to the system base
    public String add(String username,Customer customer){
        customers.customers.put(username, customer);
        customers.saveCustomers();
        return customer.firstName();
    }


    // show all customers in the system
    public void showAll(){
        customers.customers.values().stream().forEach(x -> System.out.println(x.firstName()));
    }
}
