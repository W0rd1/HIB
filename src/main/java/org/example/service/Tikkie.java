package org.example.service;

import org.example.model.Customer;
import org.example.model.PaymentRequest;
import org.example.model.RequestStatus;
import org.example.repository.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

public class Tikkie {
    private static final Data data = new Data();

    public static void sendRequest(
            Data data,
            Customer customer,
            String reciverUsername,
            String description,
            BigDecimal amount,
            boolean editale){

        UUID SenderID = customer.id();
        UUID RequestID = UUID.randomUUID();
        String SenderUsername = customer.username();
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime expiresAt = createdAt.plusHours(2);


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

        data.inbox.computeIfAbsent(reciverUsername, key -> new ArrayList<>())
                .add(request);

    }
}
