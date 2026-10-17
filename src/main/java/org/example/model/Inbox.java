package org.example.model;

import java.math.BigDecimal;
import java.util.UUID;

public record Inbox(
        UUID id,
        String from,
        String description,
        BigDecimal amoutn,
        RequestStatus status,
        boolean editable
) {
}
