package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentRequest(
        UUID requesterId,
        UUID senderId,
        String sender_username,
        String description,
        BigDecimal amount,
        RequestStatus status,
        boolean editable,
        LocalDateTime createdAt,
        LocalDateTime expiresAt
) {
}
