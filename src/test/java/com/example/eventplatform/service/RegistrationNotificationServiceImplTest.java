package com.example.eventplatform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.example.eventplatform.config.NotificationProperties;
import com.example.eventplatform.dto.ParticipantRegistrationRequest;
import com.example.eventplatform.entity.Event;
import com.example.eventplatform.entity.Participant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class RegistrationNotificationServiceImplTest {

    private static final String FROM = "noreply@mail.nic.ru";

    @Mock
    private JavaMailSender mailSender;

    private RegistrationNotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new RegistrationNotificationServiceImpl(
                mailSender,
                new NotificationProperties(true, FROM)
        );
    }

    @ParameterizedTest(name = "отправляет письмо на {0}")
    @ValueSource(strings = {
            "user@mail.ru",
            "user@yandex.ru",
            "user@rambler.ru",
            "user@inbox.ru",
            "employee@gazprom.ru",
            "employee@gazprombank.ru",
            "employee@sber.ru",
            "employee@vtb.ru",
            "qqqq@it-one.ru",
            "dev@company.gazprom.ru"
    })
    @DisplayName("Нотификация не фильтрует домен получателя")
    void sendsNotificationToAnyRussianDomain(String recipientEmail) {
        Participant participant = Participant.builder()
                .id(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .firstName("Иван")
                .lastName("Иванов")
                .email(recipientEmail)
                .company("Компания")
                .projectRole("Разработчик")
                .stack("Java")
                .grade("Middle")
                .build();

        ParticipantRegistrationRequest request = new ParticipantRegistrationRequest(
                "Иван",
                "Иванов",
                recipientEmail,
                "Компания",
                "Разработчик",
                "Java",
                "Middle",
                "@ivan",
                List.of(UUID.fromString("11111111-1111-1111-1111-111111111111")),
                true,
                true,
                false
        );

        Event event = Event.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .title("Митап")
                .eventDate(LocalDate.of(2026, 8, 15))
                .registrationEnabled(true)
                .build();

        notificationService.notifyNewRegistration(participant, request, List.of(event));

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();
        assertThat(message.getFrom()).isEqualTo(FROM);
        assertThat(message.getTo()).containsExactly(recipientEmail);
        assertThat(message.getSubject()).contains("Иван");
        assertThat(message.getText()).contains(recipientEmail);
        assertThat(message.getText()).contains("Митап");
    }
}
