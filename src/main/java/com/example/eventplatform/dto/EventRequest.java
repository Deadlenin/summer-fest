package com.example.eventplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record EventRequest(
        @NotBlank String title,
        String description,
        String extendedDescription,
        @NotNull LocalDate eventDate,
        @NotNull Boolean registrationEnabled,
        Integer sortOrder
) {
}
