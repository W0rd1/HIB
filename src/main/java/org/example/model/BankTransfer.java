package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BankTransfer(
        UUID id,
        UUID senderId,
        UUID receiverId,
        BigDecimal amount,
        String description,
        TransferStatus status,
        LocalDateTime createdAt
) {
}
