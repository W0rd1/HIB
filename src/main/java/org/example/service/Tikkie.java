package org.example.service;

import org.example.model.Customer;
import org.example.model.PaymentRequest;
import org.example.model.RequestStatus;
import org.example.repository.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Tikkie {
    private static final Data data = new Data();
    private static final ScheduledExecutorService expiryTimer = Executors.newSingleThreadScheduledExecutor(task -> {
        Thread thread = new Thread(task, "payment-request-expiry");
        thread.setDaemon(true);
        return thread;
    });

    public static void sendRequest(
            Data data,
            Customer customer,
            String reciverUsername,
            String description,
            BigDecimal amount,
            boolean editale){

        Customer currentCustomer = data.findCustomer(customer.username());
        if (currentCustomer == null || !currentCustomer.active()) {
            throw new IllegalArgumentException("Your account is inactive. Payment requests are unavailable.");
        }
        Customer receiver = data.findCustomer(reciverUsername);
        if (receiver == null) {
            throw new IllegalArgumentException("No customer found with that username.");
        }
        if (!receiver.active()) {
            throw new IllegalArgumentException("The recipient's account is inactive and cannot receive a request.");
        }

        UUID SenderID = currentCustomer.id();
        UUID RequestID = UUID.randomUUID();
        String SenderUsername = currentCustomer.username();
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime expiresAt = createdAt.plusMinutes(30);

        PaymentRequest request = new PaymentRequest(
                RequestID,
                SenderID,
                SenderUsername,
                description,
                amount,
                RequestStatus.PENDING,
                editale,
                createdAt,
                expiresAt
        );

        data.inbox.computeIfAbsent(receiver.username(), key -> new ArrayList<>())
                .add(request);
        data.saveInbox();
        expiryTimer.schedule(data::expireRequests, 30, TimeUnit.MINUTES);
    }

    public static void handleRequest(Scanner scanner, Customer customer, Data data){
        data.expireRequests();
        List<PaymentRequest> inbox = data.inbox.get(customer.username());
        if (inbox == null || inbox.isEmpty()) return;

        System.out.print(ConsoleStyle.prompt("Enter request ID (or 0 to return): "));
        String requestId = scanner.nextLine().trim();
        if (requestId.equals("0")) return;

        PaymentRequest request;
        try {
            UUID id = UUID.fromString(requestId);
            request = inbox.stream().filter(item -> item.requesterId().equals(id)).findFirst().orElse(null);
        } catch (IllegalArgumentException e) {
            request = null;
        }

        if (request == null) {
            System.out.println(ConsoleStyle.error("No request found with that ID."));
            return;
        }
        if (request.status() != RequestStatus.PENDING) {
            System.out.println(request.status() == RequestStatus.EXPIRED
                    ? ConsoleStyle.error("This request has expired and can no longer be approved.")
                    : ConsoleStyle.error("This request has already been handled."));
            return;
        }
        Customer currentCustomer = data.customers.get(customer.username());
        editRequest(scanner, data, currentCustomer == null ? customer : currentCustomer, request);
    }

    private static void editRequest(Scanner scanner, Data data, Customer customer, PaymentRequest request){
        UUID selectedRequestId = request.requesterId();
        System.out.print(ConsoleStyle.prompt("Approve this payment request? (yes/no): "));
        String answer = scanner.nextLine().trim();
        data.expireRequests();
        PaymentRequest currentRequest = data.inbox.getOrDefault(customer.username(), List.of()).stream()
                .filter(item -> item.requesterId().equals(selectedRequestId)).findFirst().orElse(null);
        if (currentRequest == null || currentRequest.status() != RequestStatus.PENDING) {
            System.out.println(ConsoleStyle.error("This request has expired or has already been handled."));
            return;
        }
        request = currentRequest;
        if (answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("y")) {
            UUID requestingCustomerId = request.senderId();
            Customer requester = data.customers.values().stream()
                    .filter(item -> item.id().equals(requestingCustomerId)).findFirst().orElse(null);
            if (requester == null) {
                System.out.println(ConsoleStyle.error("The requesting customer could not be found. No money was transferred."));
                return;
            }
            if (customer.balance().compareTo(request.amount()) < 0) {
                System.out.println(ConsoleStyle.error("Insufficient balance. No money was transferred."));
                return;
            }
            if (!customer.active() || customer.frozen()) {
                System.out.println(ConsoleStyle.error("Your account is inactive or frozen. No money was transferred."));
                return;
            }
            if (!requester.active() || requester.frozen()) {
                System.out.println(ConsoleStyle.error("The requesting account is inactive or frozen. No money was transferred."));
                return;
            }

            Customer updatedPayer = withBalance(customer, customer.balance().subtract(request.amount()));
            Customer updatedRequester = withBalance(requester, requester.balance().add(request.amount()));
            data.customers.put(updatedPayer.username(), updatedPayer);
            data.customers.put(updatedRequester.username(), updatedRequester);
            replaceRequest(data, customer.username(), request, withStatus(request, RequestStatus.PAID));
            data.saveCustomers();
            data.saveInbox();
            System.out.println(ConsoleStyle.success("Payment request approved. €" + request.amount()
                    + " was deducted from your balance and sent to " + requester.username() + "."));
        } else if (answer.equalsIgnoreCase("no") || answer.equalsIgnoreCase("n")) {
            replaceRequest(data, customer.username(), request, withStatus(request, RequestStatus.REJECTED));
            LocalDateTime now = LocalDateTime.now();
            data.inbox.computeIfAbsent(request.sender_username(), key -> new ArrayList<>()).add(
                    new PaymentRequest(UUID.randomUUID(), customer.id(), customer.username(),
                            "Your payment request to " + customer.username() + " was rejected.", request.amount(),
                            RequestStatus.REJECTED, false, now, now));
            data.saveInbox();
            System.out.println(ConsoleStyle.success("Payment request rejected. The requester has been notified."));
        } else {
            System.out.println(ConsoleStyle.error("Please answer yes or no. The request was not changed."));
        }
    }

    private static Customer withBalance(Customer customer, BigDecimal balance) {
        return new Customer(customer.id(), customer.firstName(), customer.lastName(), customer.email(),
                customer.phoneNumber(), customer.address(), customer.country(), customer.username(),
                customer.password(), balance, customer.active(), customer.frozen());
    }

    private static PaymentRequest withStatus(PaymentRequest request, RequestStatus status) {
        return new PaymentRequest(request.requesterId(), request.senderId(), request.sender_username(),
                request.description(), request.amount(), status, request.editable(), request.createdAt(), request.expiresAt());
    }

    private static void replaceRequest(Data data, String username, PaymentRequest oldRequest, PaymentRequest updatedRequest) {
        List<PaymentRequest> inbox = data.inbox.get(username);
        for (int i = 0; i < inbox.size(); i++) {
            if (inbox.get(i) == oldRequest) {
                inbox.set(i, updatedRequest);
                return;
            }
        }
    }
}
