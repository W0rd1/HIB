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
    private final static AccountAdminService accountAdminService = new AccountAdminService();
    private final static CustomerAccountService customerAccountService = new CustomerAccountService();
    private final static Data customers = new Data();
    private final static Admin admin = new Admin(System.getenv("BANK_ADMIN_USERNAME"), System.getenv("BANK_ADMIN_PASSWORD"));
    private final static Tikkie tikkieService = new Tikkie();

    public static void mainBanner() {
        Scanner scanner = new Scanner(System.in);
        boolean valid = true;
        while (valid) {
            System.out.println(ConsoleStyle.title("""
                    
                    =================================
                         WELCOME TO JAVA BANK
                    =================================
                    
                    1. Login
                    2. Register
                    0. Exit
                    
                    =================================
                    
                    Please choose an option:
                    """));
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> loginBanner(scanner);
                case "2" -> registerBanner(scanner);
                case "0" -> valid = false;
                default -> System.out.println(ConsoleStyle.error("Invalid option. Please choose 1, 2, or 0."));
            }
        }
    }


    public static void loginBanner(Scanner scanner) {
        System.out.println(ConsoleStyle.title("""
                
                =================================
                        CUSTOMER LOGIN
                =================================
                """));

        System.out.print(ConsoleStyle.prompt("Username: "));
        String username = scanner.nextLine().trim().toLowerCase();

        System.out.print(ConsoleStyle.prompt("Password: "));
        String password = scanner.nextLine().trim();

        if (admin.username().equals(username)
                && admin.password().equals(password)) {
            adminMenu(scanner);
        } else if (customers.findCustomer(username) != null) {
            Customer customer = customers.findCustomer(username);
            if (!customer.active()) {
                System.out.println(ConsoleStyle.error("This account is inactive. Please contact the bank administrator."));
            } else if (customer.password().equals(password)) {
                mainMenu(scanner, customer);
            } else {
        System.out.println(ConsoleStyle.error("Username or password is not correct."));
            }
        } else {
                System.out.println(ConsoleStyle.error("Username or password is not correct."));
        }

    }

    public static void registerBanner(Scanner scanner) {
        System.out.println(ConsoleStyle.title("""
                
                =================================
                     CUSTOMER REGISTRATION
                =================================
                """));

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

        Customer customer;
        try {
            customer = systemCRUDService.register(
                    customers,
                    firstName,
                    lastName,
                    email,
                    phoneNumber,
                    address,
                    country,
                    username,
                    password
            );
        } catch (IllegalArgumentException e) {
            System.out.println(ConsoleStyle.error(e.getMessage()));
            return;
        }

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
            System.out.println(ConsoleStyle.title("""
                    
                    =================================
                           JAVA BANK DASHBOARD
                    =================================
                    1. My Account
                    2. Inbox
                    3. Send Payment Request
                    4. Deposit
                    5. Withdraw
                    0. Logout
                    =================================
                    """));

            System.out.print("Choose an option: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> showAccountDetails(customer);
                case "2" -> showInbox(scanner, customer);
                case "3" -> sendRequestBanner(scanner,customer);
                case "4" -> deposit(scanner, customer);
                case "5" -> withdraw(scanner, customer);
                case "0" -> {
                    loggedin = false;
                    System.out.println(ConsoleStyle.success("You have been logged out."));
                }
                default -> System.out.println(ConsoleStyle.error("Invalid option. Choose 1, 2, 3, 4, 5, or 0."));
            }
        }
    }

    private static void showAccountDetails(Customer customer) {
        customer = customers.customers.getOrDefault(customer.username(), customer);
        System.out.println(ConsoleStyle.title("""
                
                ==========================================
                          JAVA BANK - MY ACCOUNT
                ==========================================
                """));

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

    private static void showInbox(Scanner scanner, Customer customer) {
        if (customer == null) {
            return;
        }

        customers.expireRequests();
        List<PaymentRequest> inbox = customers.inbox.get(customer.username());

        System.out.println(ConsoleStyle.title("""

            ==========================================
                         YOUR INBOX
            ==========================================
            """));

        if (inbox == null || inbox.isEmpty()) {
            System.out.println(ConsoleStyle.success("       You have no payment requests."));
            System.out.println("==========================================");
            return;
        }

        for (int i = 0; i < inbox.size(); i++) {
            PaymentRequest message = inbox.get(i);

            System.out.printf("""

                ------------------------------------------
                Request ID : %s
                ------------------------------------------
                From        : %s
                Description : %s
                Amount      : €%s
                Status      : %s
                Editable    : %s
                ------------------------------------------
                """,
                    ConsoleStyle.highlight(message.requesterId().toString()),
                    message.sender_username(),
                    message.description(),
                    message.amount(),
                    statusLabel(message),
                    message.editable()
            );
        }

        System.out.println(ConsoleStyle.title("""

            ==========================================
              Enter a request ID to handle it.
              Enter 0 to return to the menu.
            ==========================================
            """));
        Tikkie.handleRequest(scanner, customer, customers);
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
            System.out.println(ConsoleStyle.error("Error: Username cannot be empty."));
            return;
        }

        if (receiverUsername.equalsIgnoreCase(customer.username())) {
            System.out.println(ConsoleStyle.error("Error: You cannot request money from yourself."));
            return;
        }

        System.out.print("Description: ");
        String description = scanner.nextLine().trim();

        if (description.isBlank()) {
            System.out.println(ConsoleStyle.error("Error: Description cannot be empty."));
            return;
        }

        BigDecimal amount;
        try {
            System.out.print("Amount (€): ");
            amount = new BigDecimal(scanner.nextLine().trim());

            if (amount.scale() > 2 || amount.signum() <= 0) {
                System.out.println(ConsoleStyle.error("Error: Enter a positive amount with up to 2 decimal places."));
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println(ConsoleStyle.error("Error: Please enter a valid amount."));
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

            System.out.println(ConsoleStyle.success("Payment request sent successfully."));
        } catch (IllegalArgumentException e) {
            System.out.println(ConsoleStyle.error("Error: " + e.getMessage()));
        }

        System.out.println("==========================================");
    }

    private static void deposit(Scanner scanner, Customer customer) {
        System.out.print(ConsoleStyle.prompt("Deposit amount (€): "));
        try {
            BigDecimal amount = new BigDecimal(scanner.nextLine().trim());
            Customer updated = customerAccountService.deposit(customers, customer.username(), amount);
            System.out.println(ConsoleStyle.success("Deposit complete. New balance: €" + updated.balance()));
        } catch (NumberFormatException e) {
            System.out.println(ConsoleStyle.error("Enter a valid amount."));
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println(ConsoleStyle.error(e.getMessage()));
        }
    }

    private static void withdraw(Scanner scanner, Customer customer) {
        System.out.print(ConsoleStyle.prompt("Withdrawal amount (€): "));
        try {
            BigDecimal amount = new BigDecimal(scanner.nextLine().trim());
            Customer updated = customerAccountService.withdraw(customers, customer.username(), amount);
            System.out.println(ConsoleStyle.success("Withdrawal complete. New balance: €" + updated.balance()));
        } catch (NumberFormatException e) {
            System.out.println(ConsoleStyle.error("Enter a valid amount."));
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println(ConsoleStyle.error(e.getMessage()));
        }
    }

    private static void adminMenu(Scanner scanner) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println(ConsoleStyle.title("""
                    
                    =================================
                           JAVA BANK - ADMIN
                    =================================
                    1. Charge Customer Account
                    2. Freeze / Unfreeze Account
                    3. Activate / Deactivate Account
                    0. Logout
                    =================================
                    """));

            System.out.print("Choose an option: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> chargeCustomer(scanner);
                case "2" -> freezeAccount(scanner);
                case "3" -> changeAccountStatus(scanner);
                case "0" -> {
                    loggedIn = false;
                    System.out.println(ConsoleStyle.success("Admin logged out."));
                }
                default -> System.out.println(ConsoleStyle.error("Invalid option. Choose 1, 2, 3, or 0."));
            }
        }
    }

    private static void chargeCustomer(Scanner scanner) {
        System.out.println("""

            =================================
                 CHARGE CUSTOMER ACCOUNT
            =================================
            """);

        System.out.print(ConsoleStyle.prompt("Enter customer username: "));
        String username = scanner.nextLine().trim();
        System.out.print(ConsoleStyle.prompt("Enter customer ID: "));
        String ID = scanner.nextLine().trim();
        System.out.print(ConsoleStyle.prompt("Enter amount (€): "));
        String amount = scanner.nextLine().trim();

        try {
            Customer updatedCustomer = accountAdminService.charge(customers, username, ID, new BigDecimal(amount));
            System.out.println(ConsoleStyle.success("Customer charged successfully."));
            System.out.println(ConsoleStyle.success("New balance: €" + updatedCustomer.balance()));
        } catch (NumberFormatException e) {
            System.out.println(ConsoleStyle.error("Enter a valid amount."));
        } catch (IllegalArgumentException e) {
            System.out.println(ConsoleStyle.error(e.getMessage()));
        }
    }

    private static void freezeAccount(Scanner scanner) {
        System.out.print(ConsoleStyle.prompt("Enter customer username: "));
        String username = scanner.nextLine().trim();
        Customer customer = customers.findCustomer(username);
        if (customer == null) {
            System.out.println(ConsoleStyle.error("No customer found with that username."));
            return;
        }
        Customer updated = accountAdminService.setFrozen(customers, customer.username(), !customer.frozen());
        System.out.println(updated.frozen()
                ? ConsoleStyle.success("Account frozen. Deposits and withdrawals are disabled.")
                : ConsoleStyle.success("Account unfrozen. Deposits and withdrawals are enabled."));
    }

    private static void changeAccountStatus(Scanner scanner) {
        System.out.print(ConsoleStyle.prompt("Enter customer username: "));
        String username = scanner.nextLine().trim();
        Customer customer = customers.findCustomer(username);
        if (customer == null) {
            System.out.println(ConsoleStyle.error("No customer found with that username."));
            return;
        }
        Customer updated = accountAdminService.setActive(customers, customer.username(), !customer.active());
        System.out.println(updated.active()
                ? ConsoleStyle.success("Account activated. The customer can log in and send payment requests.")
                : ConsoleStyle.success("Account deactivated. Login and payment requests are disabled."));
    }

    private static String statusLabel(PaymentRequest request) {
        return switch (request.status()) {
            case PENDING -> ConsoleStyle.prompt(request.status().toString());
            case PAID -> ConsoleStyle.success(request.status().toString());
            case REJECTED, EXPIRED, CANCELLED -> ConsoleStyle.error(request.status().toString());
        };
    }

}
