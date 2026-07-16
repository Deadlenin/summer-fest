package com.example.eventplatform.service;

import com.example.eventplatform.aspect.LogEmailNotification;
import com.example.eventplatform.config.NotificationProperties;
import com.example.eventplatform.dto.ParticipantRegistrationRequest;
import com.example.eventplatform.entity.Event;
import com.example.eventplatform.entity.Participant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegistrationNotificationServiceImpl implements RegistrationNotificationService {

    private static final String CONFIRMATION_TEXT =
            "Спасибо, твоя заявка на Летний ТехФест 2026 принята.";

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
        message.setSubject(CONFIRMATION_TEXT);
        message.setText(CONFIRMATION_TEXT);
        mailSender.send(message);
    }
}
