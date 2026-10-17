package org.example.model;

import java.math.BigDecimal;
import java.util.UUID;

public record Customer(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String address,
        String country,
        String username,
        String password,
        BigDecimal balance,
        boolean active,
        boolean frozen
) {
}

