package com.example.eventplatform.event;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.example.eventplatform.dto.ParticipantRegistrationRequest;
import com.example.eventplatform.entity.Event;
import com.example.eventplatform.entity.Participant;
import com.example.eventplatform.service.RegistrationNotificationService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Notification is triggered by transactional listener, not by ParticipantRegistrationServiceImpl.
 * This class covers the NotificationService failure boundary requested for registration flow.
 */
@ExtendWith(MockitoExtension.class)
class ParticipantRegisteredEventListenerTest {

    @Mock
    private RegistrationNotificationService registrationNotificationService;

    @InjectMocks
    private ParticipantRegisteredEventListener listener;

    @Test
    @DisplayName("Ошибка NotificationService пробрасывается из listener")
    void shouldPropagateNotificationException() {
        // Arrange
        Participant participant = Participant.builder()
                .id(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .firstName("Иван")
                .lastName("Иванов")
                .email("user@mail.ru")
                .company("ИнфоТеКС")
                .projectRole("Разработчик")
                .stack("Java")
                .grade("Middle")
                .personalDataConsent(true)
                .photoConsent(true)
                .newsletterConsent(false)
                .build();

        Event event = Event.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .title("Митап")
                .eventDate(LocalDate.of(2026, 8, 15))
                .registrationEnabled(true)
                .build();

        ParticipantRegistrationRequest request = new ParticipantRegistrationRequest(
                "Иван",
                "Иванов",
                "user@mail.ru",
                "ИнфоТеКС",
                "Разработчик",
                "Java",
                "Middle",
                "@ivan",
                List.of(event.getId()),
                true,
                true,
                false
        );

        RuntimeException notificationFailure = new RuntimeException("SMTP failed");
        doThrow(notificationFailure)
                .when(registrationNotificationService)
                .notifyNewRegistration(participant, request, List.of(event));

        ParticipantRegisteredEvent registeredEvent =
                new ParticipantRegisteredEvent(participant, request, List.of(event));

        // Act / Assert
        assertThatThrownBy(() -> listener.onParticipantRegistered(registeredEvent))
                .isSameAs(notificationFailure);

        verify(registrationNotificationService)
                .notifyNewRegistration(participant, request, List.of(event));
        verifyNoMoreInteractions(registrationNotificationService);
    }
}
