package org.example.service;

import org.example.model.Customer;
import org.example.model.Inbox;
import org.example.model.PaymentRequest;
import org.example.repository.Admin;
import org.example.repository.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

public class Banner {
    private final static SystemCRUD systemCRUDService = new SystemCRUD();
    private final static Data customers = new Data();
    private final static Admin admin = new Admin(System.getenv("BANK_ADMIN_USERNAME"), System.getenv("BANK_ADMIN_PASSWORD"));
    private final static Tikkie tikkieService = new Tikkie();

    public static void mainBanner() {
        Scanner scanner = new Scanner(System.in);
        boolean valid = true;
        while (valid) {
            System.out.println("""
                    
                    =================================
                         WELCOME TO JAVA BANK
                    =================================
                    
                    1. Login
                    2. Register
                    0. Exit
                    
                    =================================
                    
                    Please choice an option : 
                    """);
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> loginBanner(scanner);
                case "2" -> registerBanner(scanner);
                case "0" -> valid = false;
                default -> System.out.println("Invalid option, try again");
            }
        }
    }


    public static void loginBanner(Scanner scanner) {
        System.out.println("""
                
                =================================
                        CUSTOMER LOGIN
                =================================
                """);

        System.out.print("Username: ");
        String username = scanner.nextLine().trim().toLowerCase();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (admin.username().equals(username)
                && admin.password().equals(password)) {
            adminMenu(scanner);
        } else if (customers.customers.containsKey(username)) {
            Customer customer = customers.customers.get(username);

            if (customer.password().equals(password)) {
                mainMenu(scanner, customer);
            } else {
                System.out.println("Username or password is not correct.");
            }
        } else {
            System.out.println("Username or password is not correct.");
        }

    }

    public static void registerBanner(Scanner scanner) {
        System.out.println("""
                
                =================================
                     CUSTOMER REGISTRATION
                =================================
                """);

        System.out.print("First name: ");
        String firstName = scanner.nextLine().trim();

        System.out.print("Last name: ");
        String lastName = scanner.nextLine().trim();

        System.out.print("Email: ");
        String email = scanner.nextLine().trim().toLowerCase();

        System.out.print("Phone number: ");
        String phoneNumber = scanner.nextLine().trim();

        System.out.print("Address: ");
        String address = scanner.nextLine().trim();

        System.out.print("Country: ");
        String country = scanner.nextLine().trim();

        System.out.println("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        Customer customer = systemCRUDService.create(
                firstName,
                lastName,
                email,
                phoneNumber,
                address,
                country,
                username,
                password
        );

        customers.customers.put(customer.username(), customer);

        System.out.println("""
                
                =================================
                  REGISTRATION SUCCESSFUL!
                =================================
                """);
        System.out.println("Welcome to Java Bank, " + firstName + "!");
    }

    private static void mainMenu(Scanner scanner, Customer customer) {
        boolean loggedin = true;
        while (loggedin) {
            System.out.println("""
                    
                    =================================
                           JAVA BANK DASHBOARD
                    =================================
                    1. My Account
                    2. Inbox
                    3. Send Payment Request
                    0. Logout
                    =================================
                    """);

            System.out.print("Choose an option: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> showAccountDetails(customer);
                case "2" -> showInbox(customer);
                case "3" -> sendRequestBanner(scanner,customer);
                case "0" -> {
                    loggedin = false;
                    mainBanner();
                }
            }
        }
    }

    private static void showAccountDetails(Customer customer) {
        System.out.println("""
                
                ==========================================
                          JAVA BANK - MY ACCOUNT
                ==========================================
                """);

        System.out.printf("""
                        Customer ID : %s
                        First Name  : %s
                        Last Name   : %s
                        Email       : %s
                        Phone       : %s
                        Address     : %s
                        Country     : %s
                        Username    : %s
                        Balance     : €%s
                        Status      : %s
                        Frozen      : %s
                        ==========================================
                        """,
                customer.id(),
                customer.firstName(),
                customer.lastName(),
                customer.email(),
                customer.phoneNumber(),
                customer.address(),
                customer.country(),
                customer.username(),
                customer.balance(),
                customer.active() ? "Active" : "Inactive",
                customer.frozen() ? "Yes" : "No"
        );
    }

    private static void showInbox(Customer customer) {
        if (customer == null) {
            return;
        }

        List<PaymentRequest> inbox = customers.inbox.get(customer.username());

        System.out.println("""

            ==========================================
                         YOUR INBOX
            ==========================================
            """);

        if (inbox == null || inbox.isEmpty()) {
            System.out.println("       You have no payment requests.");
            System.out.println("==========================================");
            return;
        }

        for (int i = 0; i < inbox.size(); i++) {
            PaymentRequest message = inbox.get(i);

            System.out.printf("""

                ------------------------------------------
                Request #%d
                ------------------------------------------
                From        : %s
                Description : %s
                Amount      : €%s
                Status      : %s
                Editable    : %s
                ------------------------------------------
                """,
                    i + 1,
                    message.sender_username(),
                    message.description(),
                    message.amount(),
                    message.status(),
                    message.editable()
            );
        }

        System.out.println("""

            ==========================================
              Choose the request number to handle it.
              Enter 0 to return to the menu.
            ==========================================
            """);
    }

    private static void sendRequestBanner(Scanner scanner, Customer customer) {
        System.out.println("""

            ==========================================
                    SEND PAYMENT REQUEST
            ==========================================
            """);

        System.out.print("Receiver username: ");
        String receiverUsername = scanner.nextLine().trim();

        if (receiverUsername.isBlank()) {
            System.out.println("Error: Username cannot be empty.");
            return;
        }

        if (receiverUsername.equalsIgnoreCase(customer.username())) {
            System.out.println("Error: You cannot request money from yourself.");
            return;
        }

        System.out.print("Description: ");
        String description = scanner.nextLine().trim();

        if (description.isBlank()) {
            System.out.println("Error: Description cannot be empty.");
            return;
        }

        BigDecimal amount;
        try {
            System.out.print("Amount (€): ");
            amount = new BigDecimal(scanner.nextLine().trim());

            if (amount.scale() > 2 || amount.signum() <= 0) {
                System.out.println("Error: Enter a positive amount with up to 2 decimal places.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Please enter a valid amount.");
            return;
        }

        boolean editable;
        while (true) {
            System.out.print("Allow editing amount? (yes/no): ");
            String answer = scanner.nextLine().trim();

            if (answer.equalsIgnoreCase("yes")) {
                editable = true;
                break;
            } else if (answer.equalsIgnoreCase("no")) {
                editable = false;
                break;
            }

            System.out.println("Please enter yes or no.");
        }

        try {
            tikkieService.sendRequest(
                    customers,customer, receiverUsername, description, amount, editable
            );

            System.out.println("Payment request sent successfully!");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.println("==========================================");
    }
    private static void adminMenu(Scanner scanner) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("""
                    
                    =================================
                           JAVA BANK - ADMIN
                    =================================
                    1. Charge Customer Account
                    2. Freeze / Unfreeze Account
                    0. Logout
                    =================================
                    """);

            System.out.print("Choose an option: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> chargeCustomer(scanner);
                //case "2" -> freezeAccount(scanner);
                case "0" -> {
                    loggedIn = false;
                    System.out.println("Admin logged out.");
                }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static void chargeCustomer(Scanner scanner) {
        System.out.println("""

            =================================
                 CHARGE CUSTOMER ACCOUNT
            =================================
            """);

        System.out.print("Enter Customer Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter Customer ID: ");
        String ID = scanner.nextLine().trim();
        System.out.println("Enter amount (euro):");
        String amount = scanner.nextLine().trim();

        try{
            Customer customer = customers.customers.get(username);
            UUID CustomerUUID = UUID.fromString(ID);
            BigDecimal chargeAmount = new BigDecimal(amount);

            if (customer == null || !customer.id().equals(CustomerUUID)) {
                System.out.println("No customer found with these credentials.");
                return;
            }

            if (chargeAmount.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("Amount must be greater than zero.");
                return;
            }

            Customer updatedCustomer = new Customer(
                    customer.id(),
                    customer.firstName(),
                    customer.lastName(),
                    customer.email(),
                    customer.phoneNumber(),
                    customer.address(),
                    customer.country(),
                    customer.username(),
                    customer.password(),
                    customer.balance().add(chargeAmount),
                    customer.active(),
                    customer.frozen()
            );

            customers.customers.put(username, updatedCustomer);

            System.out.println("Customer charged successfully!");
            System.out.println("New balance: €" + updatedCustomer.balance());

        }catch (IllegalArgumentException e){
            System.out.println("Invalid Customer Credentials");
        }
    }

}
