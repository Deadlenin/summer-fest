package com.example.eventplatform.event;

import com.example.eventplatform.dto.ParticipantRegistrationRequest;
import com.example.eventplatform.entity.Event;
import com.example.eventplatform.entity.Participant;
import java.util.List;

public record ParticipantRegisteredEvent(
        Participant participant,
        ParticipantRegistrationRequest request,
        List<Event> events
) {
}
