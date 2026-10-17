package org.example.service;

import org.example.model.Customer;
import org.example.repository.Data;

import java.math.BigDecimal;

/**
 * Handles customer balance operations independently of the command-line UI.
 */
public class CustomerAccountService {

    /**
     * Deposits money into the customer's account and persists the new balance.
     *
     * @return the updated customer
     * @throws IllegalArgumentException if the amount is not positive or has more than two decimal places
     * @throws IllegalStateException if the customer does not exist or cannot transact
     */
    public Customer deposit(Data data, String username, BigDecimal amount) {
        validateAmount(amount);
        Customer customer = requireTransactableCustomer(data, username);

        Customer updatedCustomer = withBalance(customer, customer.balance().add(amount));
        data.updateCustomer(updatedCustomer);
        return updatedCustomer;
    }

    /**
     * Withdraws money from the customer's account and persists the new balance.
     *
     * @return the updated customer
     * @throws IllegalArgumentException if the amount is not positive or has more than two decimal places
     * @throws IllegalStateException if the customer does not exist, cannot transact, or has insufficient funds
     */
    public Customer withdraw(Data data, String username, BigDecimal amount) {
        validateAmount(amount);
        Customer customer = requireTransactableCustomer(data, username);
        if (customer.balance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance. No money was withdrawn.");
        }

        Customer updatedCustomer = withBalance(customer, customer.balance().subtract(amount));
        data.updateCustomer(updatedCustomer);
        return updatedCustomer;
    }

    private static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        if (amount.scale() > 2) {
            throw new IllegalArgumentException("Amount cannot have more than two decimal places.");
        }
    }

    private static Customer requireTransactableCustomer(Data data, String username) {
        if (data == null || username == null || username.isBlank()) {
            throw new IllegalArgumentException("A valid customer account is required.");
        }
        Customer customer = data.findCustomer(username);
        if (customer == null) {
            throw new IllegalStateException("Customer account was not found.");
        }
        if (!customer.active()) {
            throw new IllegalStateException("This account is inactive. Contact the bank for assistance.");
        }
        if (customer.frozen()) {
            throw new IllegalStateException("This account is frozen. Deposits and withdrawals are unavailable.");
        }
        return customer;
    }

    private static Customer withBalance(Customer customer, BigDecimal balance) {
        return new Customer(customer.id(), customer.firstName(), customer.lastName(), customer.email(),
                customer.phoneNumber(), customer.address(), customer.country(), customer.username(),
                customer.password(), balance, customer.active(), customer.frozen());
    }
}
