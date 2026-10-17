package org.example.repository;

import org.example.model.Customer;
import org.example.model.PaymentRequest;
import org.example.model.RequestStatus;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Data {
    private static final Path DATA_DIRECTORY = Path.of("data");
    private static final Path CUSTOMERS_FILE = DATA_DIRECTORY.resolve("customers.csv");
    private static final Path INBOX_FILE = DATA_DIRECTORY.resolve("payment_requests.csv");

    public final Map<String, Customer> customers = new HashMap<>();
    public final Map<String, List<PaymentRequest>> inbox = new HashMap<>();

    public Data() {
        loadCustomers();
        loadInbox();
        expireRequests();
    }

    public void expireRequests() {
        LocalDateTime now = LocalDateTime.now();
        boolean changed = false;
        for (List<PaymentRequest> requests : inbox.values()) {
            for (int i = 0; i < requests.size(); i++) {
                PaymentRequest request = requests.get(i);
                if (request.status() == RequestStatus.PENDING && !request.expiresAt().isAfter(now)) {
                    requests.set(i, new PaymentRequest(request.requesterId(), request.senderId(),
                            request.sender_username(), request.description(), request.amount(),
                            RequestStatus.EXPIRED, request.editable(), request.createdAt(), request.expiresAt()));
                    changed = true;
                }
            }
        }
        if (changed) saveInbox();
    }

    public Customer findCustomer(String username) {
        if (username == null) return null;
        return customers.values().stream()
                .filter(customer -> customer.username().equalsIgnoreCase(username.trim()))
                .findFirst().orElse(null);
    }

    public Customer findCustomerByEmail(String email) {
        if (email == null) return null;
        return customers.values().stream()
                .filter(customer -> customer.email().equalsIgnoreCase(email.trim()))
                .findFirst().orElse(null);
    }

    public void addCustomer(Customer customer) {
        customers.put(customer.username(), customer);
        saveCustomers();
    }

    public void updateCustomer(Customer customer) {
        customers.put(customer.username(), customer);
        saveCustomers();
    }

    public void saveCustomers() {
        List<String> rows = new ArrayList<>();
        rows.add("id,firstName,lastName,email,phoneNumber,address,country,username,password,balance,active,frozen");
        for (Customer c : customers.values()) {
            rows.add(row(c.id(), c.firstName(), c.lastName(), c.email(), c.phoneNumber(), c.address(),
                    c.country(), c.username(), c.password(), c.balance(), c.active(), c.frozen()));
        }
        write(CUSTOMERS_FILE, rows);
    }

    public void saveInbox() {
        List<String> rows = new ArrayList<>();
        rows.add("recipient,requesterId,senderId,senderUsername,description,amount,status,editable,createdAt,expiresAt");
        inbox.forEach((recipient, requests) -> requests.forEach(r -> rows.add(row(recipient,
                r.requesterId(), r.senderId(), r.sender_username(), r.description(), r.amount(),
                r.status(), r.editable(), r.createdAt(), r.expiresAt()))));
        write(INBOX_FILE, rows);
    }

    private void loadCustomers() {
        if (!Files.exists(CUSTOMERS_FILE)) return;
        try {
            for (String line : Files.readAllLines(CUSTOMERS_FILE).stream().skip(1).toList()) {
                if (line.isBlank()) continue;
                List<String> v = parse(line);
                Customer c = new Customer(UUID.fromString(v.get(0)), v.get(1), v.get(2), v.get(3), v.get(4),
                        v.get(5), v.get(6), v.get(7), v.get(8), new BigDecimal(v.get(9)),
                        Boolean.parseBoolean(v.get(10)), Boolean.parseBoolean(v.get(11)));
                customers.put(c.username(), c);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not read customer data from " + CUSTOMERS_FILE, e);
        }
    }

    private void loadInbox() {
        if (!Files.exists(INBOX_FILE)) return;
        try {
            for (String line : Files.readAllLines(INBOX_FILE).stream().skip(1).toList()) {
                if (line.isBlank()) continue;
                List<String> v = parse(line);
                PaymentRequest request = new PaymentRequest(UUID.fromString(v.get(1)), UUID.fromString(v.get(2)),
                        v.get(3), v.get(4), new BigDecimal(v.get(5)), RequestStatus.valueOf(v.get(6)),
                        Boolean.parseBoolean(v.get(7)), LocalDateTime.parse(v.get(8)), LocalDateTime.parse(v.get(9)));
                inbox.computeIfAbsent(v.get(0), key -> new ArrayList<>()).add(request);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not read payment request data from " + INBOX_FILE, e);
        }
    }

    private static void write(Path path, List<String> rows) {
        try {
            Files.createDirectories(DATA_DIRECTORY);
            Files.write(path, rows);
        } catch (IOException e) {
            throw new IllegalStateException("Could not write bank data to " + path, e);
        }
    }

    private static String row(Object... values) {
        List<String> fields = new ArrayList<>();
        for (Object value : values) {
            String s = String.valueOf(value);
            fields.add("\"" + s.replace("\"", "\"\"") + "\"");
        }
        return String.join(",", fields);
    }

    private static List<String> parse(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else quoted = !quoted;
            } else if (ch == ',' && !quoted) {
                fields.add(field.toString());
                field.setLength(0);
            } else field.append(ch);
        }
        fields.add(field.toString());
        return fields;
    }
}
