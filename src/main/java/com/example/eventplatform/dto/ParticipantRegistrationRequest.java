package com.example.eventplatform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record ParticipantRegistrationRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email @NotBlank String email,
        @NotBlank String company,
        @NotBlank String projectRole,
        @NotBlank String stack,
        String grade,
        String telegram,
        @NotEmpty List<@NotNull UUID> eventIds
) {
}
