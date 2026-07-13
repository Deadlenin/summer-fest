package com.example.eventplatform.dto;

import java.time.LocalDate;
import java.util.UUID;

public record EventResponse(
        UUID id,
        String title,
        String description,
        LocalDate eventDate,
        String companyName,
        String location
) {
}
