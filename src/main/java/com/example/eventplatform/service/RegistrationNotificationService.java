package com.example.eventplatform.service;

import com.example.eventplatform.dto.ParticipantRegistrationRequest;
import com.example.eventplatform.entity.Event;
import com.example.eventplatform.entity.Participant;
import java.util.List;

public interface RegistrationNotificationService {

    void notifyNewRegistration(
            Participant participant,
            ParticipantRegistrationRequest request,
            List<Event> events
    );
}
