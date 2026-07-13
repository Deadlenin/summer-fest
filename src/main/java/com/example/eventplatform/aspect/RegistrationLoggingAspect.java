package com.example.eventplatform.aspect;

import com.example.eventplatform.config.NotificationProperties;
import com.example.eventplatform.dto.ParticipantRegistrationRequest;
import com.example.eventplatform.entity.Participant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RegistrationLoggingAspect {

    private final NotificationProperties notificationProperties;

    @Around("@annotation(LogRegistration)")
    public Object logRegistration(ProceedingJoinPoint joinPoint) throws Throwable {
        ParticipantRegistrationRequest request = (ParticipantRegistrationRequest) joinPoint.getArgs()[0];

        log.info(
                "Registration started: email={}, firstName={}, lastName={}, eventIds={}",
                request.email(),
                request.firstName(),
                request.lastName(),
                request.eventIds()
        );

        long startedAt = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            UUID participantId = (UUID) result;

            log.info(
                    "Registration completed: participantId={}, email={}, durationMs={}",
                    participantId,
                    request.email(),
                    System.currentTimeMillis() - startedAt
            );

            return result;
        } catch (Throwable exception) {
            log.error(
                    "Registration failed: email={}, eventIds={}, durationMs={}",
                    request.email(),
                    request.eventIds(),
                    System.currentTimeMillis() - startedAt,
                    exception
            );
            throw exception;
        }
    }

    @Around("@annotation(LogEmailNotification)")
    public void logEmailNotification(ProceedingJoinPoint joinPoint) {
        Participant participant = (Participant) joinPoint.getArgs()[0];
        ParticipantRegistrationRequest request = (ParticipantRegistrationRequest) joinPoint.getArgs()[1];
        @SuppressWarnings("unchecked")
        List<?> events = (List<?>) joinPoint.getArgs()[2];

        if (!notificationProperties.enabled()) {
            log.info(
                    "Email notification skipped: participantId={}, email={}, reason=disabled",
                    participant.getId(),
                    request.email()
            );
            return;
        }

        log.info(
                "Email notification started: participantId={}, participantEmail={}, recipient={}, eventsCount={}",
                participant.getId(),
                request.email(),
                notificationProperties.recipient(),
                events.size()
        );

        long startedAt = System.currentTimeMillis();

        try {
            joinPoint.proceed();

            log.info(
                    "Email notification sent: participantId={}, participantEmail={}, recipient={}, durationMs={}",
                    participant.getId(),
                    request.email(),
                    notificationProperties.recipient(),
                    System.currentTimeMillis() - startedAt
            );
        } catch (Throwable exception) {
            log.error(
                    "Email notification failed: participantId={}, participantEmail={}, recipient={}, durationMs={}",
                    participant.getId(),
                    request.email(),
                    notificationProperties.recipient(),
                    System.currentTimeMillis() - startedAt,
                    exception
            );
        }
    }
}
