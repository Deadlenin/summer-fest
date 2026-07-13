package com.example.eventplatform.service;

import com.example.eventplatform.aspect.LogEmailNotification;
import com.example.eventplatform.config.NotificationProperties;
import com.example.eventplatform.dto.ParticipantRegistrationRequest;
import com.example.eventplatform.entity.Event;
import com.example.eventplatform.entity.Participant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class RegistrationNotificationServiceImpl implements RegistrationNotificationService {

    private static final DateTimeFormatter EVENT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final JavaMailSender mailSender;
    private final NotificationProperties notificationProperties;

    @Override
    @LogEmailNotification
    public void notifyNewRegistration(
            Participant participant,
            ParticipantRegistrationRequest request,
            List<Event> events
    ) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(notificationProperties.from());
        message.setTo(request.email());
        message.setSubject(buildSubject(request));
        message.setText(buildBody(participant, request, events));
        mailSender.send(message);
    }

    private String buildSubject(ParticipantRegistrationRequest request) {
        return "Ваша заявка принята: " + request.firstName() + " " + request.lastName();
    }

    private String buildBody(
            Participant participant,
            ParticipantRegistrationRequest request,
            List<Event> events
    ) {
        String eventsText = events.stream()
                .map(this::formatEvent)
                .collect(Collectors.joining("\n"));

        return """
                Здравствуйте, %s!

                Ваша заявка на мероприятие принята. Данные заявки:

                Имя: %s
                Фамилия: %s
                Email: %s
                Компания: %s
                Роль: %s
                Стек: %s
                Грейд: %s
                Telegram: %s
                Мероприятия:
                %s
                ID участника: %s
                """.formatted(
                request.firstName(),
                request.firstName(),
                request.lastName(),
                request.email(),
                request.company(),
                request.projectRole(),
                request.stack(),
                formatOptional(request.grade()),
                formatOptional(request.telegram()),
                eventsText,
                participant.getId()
        );
    }

    private String formatEvent(Event event) {
        if (event.getEventDate() == null) {
            return "- " + event.getTitle();
        }

        return "- " + event.getTitle() + " (" + event.getEventDate().format(EVENT_DATE_FORMATTER) + ")";
    }

    private String formatOptional(String value) {
        return StringUtils.hasText(value) ? value : "—";
    }
}
