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
    public Customer create(String fn, String ln, String email, String phone, String address, String country, String username, String password){
        UUID CustomerID = UUID.randomUUID();
        BigDecimal balance = BigDecimal.ZERO;
        return new Customer(
                CustomerID,
                fn,
                ln,
                email,
                phone,
                address,
                country,
                username,
                password,
                balance,
                false,
                true);
    }

    // This method will add the customer to the system base
    public String add(String username,Customer customer){
        customers.customers.put(username, customer);
        return customer.firstName();
    }


    // show all customers in the system
    public void showAll(){
        customers.customers.values().stream().forEach(x -> System.out.println(x.firstName()));
    }
}
