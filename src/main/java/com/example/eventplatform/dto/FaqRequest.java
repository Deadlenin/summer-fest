package com.example.eventplatform.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FaqRequest(
        @NotBlank String question,
        @NotBlank String answer,
        @NotNull @Min(0) Integer sortOrder,
        @NotNull Boolean isActive
) {
}
