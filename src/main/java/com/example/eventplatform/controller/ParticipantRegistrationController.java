package com.example.eventplatform.controller;

import com.example.eventplatform.dto.ParticipantRegistrationRequest;
import com.example.eventplatform.service.ParticipantRegistrationService;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/participants")
@RequiredArgsConstructor
public class ParticipantRegistrationController {

    private final ParticipantRegistrationService participantRegistrationService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, UUID>> register(@Valid @RequestBody ParticipantRegistrationRequest request) {
        UUID participantId = participantRegistrationService.register(request);
        return ResponseEntity.ok(Map.of("participantId", participantId));
    }
}
