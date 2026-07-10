package com.example.eventplatform.service;

import com.example.eventplatform.dto.ParticipantRegistrationRequest;
import java.util.UUID;

public interface ParticipantRegistrationService {

    UUID register(ParticipantRegistrationRequest request);
}
