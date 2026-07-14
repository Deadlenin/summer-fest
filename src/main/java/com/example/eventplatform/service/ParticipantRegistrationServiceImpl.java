package com.example.eventplatform.service;

import com.example.eventplatform.aspect.LogRegistration;
import com.example.eventplatform.dto.ParticipantRegistrationRequest;
import com.example.eventplatform.entity.Event;
import com.example.eventplatform.entity.Participant;
import com.example.eventplatform.entity.ParticipantEvent;
import com.example.eventplatform.exception.ResourceNotFoundException;
import com.example.eventplatform.repository.EventRepository;
import com.example.eventplatform.repository.ParticipantEventRepository;
import com.example.eventplatform.repository.ParticipantRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParticipantRegistrationServiceImpl implements ParticipantRegistrationService {

    private final ParticipantRepository participantRepository;
    private final EventRepository eventRepository;
    private final ParticipantEventRepository participantEventRepository;
    private final RegistrationNotificationService registrationNotificationService;

    @Override
    @Transactional
    @LogRegistration
    public UUID register(ParticipantRegistrationRequest request) {
        List<Event> events = eventRepository.findAllById(request.eventIds());
        validateAllEventsExist(request.eventIds(), events);

        Participant participant = participantRepository.findByEmail(request.email())
                .map(existing -> updateParticipant(existing, request))
                .orElseGet(() -> buildParticipant(request));

        Participant savedParticipant = participantRepository.save(participant);
        addMissingEventLinks(savedParticipant, events);
        registrationNotificationService.notifyNewRegistration(savedParticipant, request, events);

        return savedParticipant.getId();
    }

    private void validateAllEventsExist(List<UUID> requestedEventIds, List<Event> events) {
        Set<UUID> foundIds = events.stream()
                .map(Event::getId)
                .collect(java.util.stream.Collectors.toSet());

        List<UUID> missingIds = requestedEventIds.stream()
                .filter(eventId -> !foundIds.contains(eventId))
                .distinct()
                .toList();

        if (!missingIds.isEmpty()) {
            throw new ResourceNotFoundException("Events not found: " + missingIds);
        }
    }

    private Participant updateParticipant(Participant participant, ParticipantRegistrationRequest request) {
        participant.setFirstName(request.firstName());
        participant.setLastName(request.lastName());
        participant.setEmail(request.email());
        participant.setCompany(request.company());
        participant.setProjectRole(request.projectRole());
        participant.setStack(request.stack());
        participant.setGrade(request.grade());
        participant.setTelegram(request.telegram());
        participant.setPersonalDataConsent(request.personalDataConsent());
        participant.setPhotoConsent(request.photoConsent());
        participant.setNewsletterConsent(request.newsletterConsent());
        return participant;
    }

    private Participant buildParticipant(ParticipantRegistrationRequest request) {
        return Participant.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .company(request.company())
                .projectRole(request.projectRole())
                .stack(request.stack())
                .grade(request.grade())
                .telegram(request.telegram())
                .personalDataConsent(request.personalDataConsent())
                .photoConsent(request.photoConsent())
                .newsletterConsent(request.newsletterConsent())
                .build();
    }

    private void addMissingEventLinks(Participant participant, List<Event> events) {
        Set<UUID> existingEventIds = new HashSet<>(participantEventRepository.findAllByParticipantId(participant.getId())
                .stream()
                .map(participantEvent -> participantEvent.getEvent().getId())
                .toList());

        List<ParticipantEvent> newLinks = events.stream()
                .filter(event -> existingEventIds.add(event.getId()))
                .map(event -> ParticipantEvent.builder()
                        .participant(participant)
                        .event(event)
                        .build())
                .toList();

        if (!newLinks.isEmpty()) {
            participantEventRepository.saveAll(newLinks);
        }
    }
}
