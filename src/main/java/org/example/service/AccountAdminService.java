package org.example.service;

import org.example.model.Customer;
import org.example.repository.Data;

import java.math.BigDecimal;
import java.util.UUID;

public class AccountAdminService {
    public Customer setFrozen(Data data, String username, boolean frozen) {
        Customer customer = requireCustomer(data, username);
        Customer updated = copy(customer, customer.balance(), customer.active(), frozen);
        data.updateCustomer(updated);
        return updated;
    }

    public Customer setActive(Data data, String username, boolean active) {
        Customer customer = requireCustomer(data, username);
        Customer updated = copy(customer, customer.balance(), active, customer.frozen());
        data.updateCustomer(updated);
        return updated;
    }

    public Customer charge(Data data, String username, String customerId, BigDecimal amount) {
        Customer customer = requireCustomer(data, username);
        UUID id;
        try {
            id = UUID.fromString(customerId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Enter a valid customer ID.");
        }
        if (!customer.id().equals(id)) {
            throw new IllegalArgumentException("The username and customer ID do not match.");
        }
        validateAmount(amount);
        Customer updated = copy(customer, customer.balance().add(amount), customer.active(), customer.frozen());
        data.updateCustomer(updated);
        return updated;
    }

    private Customer requireCustomer(Data data, String username) {
        Customer customer = data.findCustomer(username);
        if (customer == null) throw new IllegalArgumentException("No customer found with username '" + username + "'.");
        return customer;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0 || amount.scale() > 2) {
            throw new IllegalArgumentException("Amount must be positive and have no more than 2 decimal places.");
        }
    }

    private Customer copy(Customer customer, BigDecimal balance, boolean active, boolean frozen) {
        return new Customer(customer.id(), customer.firstName(), customer.lastName(), customer.email(),
                customer.phoneNumber(), customer.address(), customer.country(), customer.username(),
                customer.password(), balance, active, frozen);
    }
}
