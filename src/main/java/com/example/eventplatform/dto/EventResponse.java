package com.example.eventplatform.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventResponse(
        UUID id,
        String title,
        String description,
        String extendedDescription,
        LocalDate eventDate,
        String companyName,
        String location,
        boolean registrationEnabled,
        Integer sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
