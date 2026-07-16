package com.example.eventplatform.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FaqResponse(
        UUID id,
        String question,
        String answer,
        Integer sortOrder,
        boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
