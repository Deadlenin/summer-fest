package com.example.eventplatform.event;

import com.example.eventplatform.service.RegistrationNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ParticipantRegisteredEventListener {

    private final RegistrationNotificationService registrationNotificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onParticipantRegistered(ParticipantRegisteredEvent event) {
        registrationNotificationService.notifyNewRegistration(
                event.participant(),
                event.request(),
                event.events()
        );
    }
}
