package com.example.eventplatform.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.eventplatform.dto.ParticipantRegistrationRequest;
import com.example.eventplatform.entity.Event;
import com.example.eventplatform.entity.Participant;
import com.example.eventplatform.entity.ParticipantEvent;
import com.example.eventplatform.event.ParticipantRegisteredEvent;
import com.example.eventplatform.exception.ResourceNotFoundException;
import com.example.eventplatform.repository.EventRepository;
import com.example.eventplatform.repository.ParticipantEventRepository;
import com.example.eventplatform.repository.ParticipantRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ParticipantRegistrationServiceImplTest {

    private static final UUID EVENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_EVENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID PARTICIPANT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final String EMAIL = "user@mail.ru";

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantEventRepository participantEventRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ParticipantRegistrationServiceImpl registrationService;

    @Captor
    private ArgumentCaptor<Participant> participantCaptor;

    @Captor
    private ArgumentCaptor<List<ParticipantEvent>> participantEventsCaptor;

    @Captor
    private ArgumentCaptor<ParticipantRegisteredEvent> registeredEventCaptor;

    private Event openEvent;
    private ParticipantRegistrationRequest request;

    @BeforeEach
    void setUp() {
        openEvent = Event.builder()
                .id(EVENT_ID)
                .title("Митап")
                .eventDate(LocalDate.of(2026, 8, 15))
                .registrationEnabled(true)
                .build();

        request = new ParticipantRegistrationRequest(
                "Иван",
                "Иванов",
                EMAIL,
                "ИнфоТеКС",
                "Разработчик",
                "Java",
                "Middle",
                "@ivan",
                List.of(EVENT_ID),
                true,
                true,
                false
        );
    }

    @Test
    @DisplayName("Успешная регистрация нового участника")
    void shouldRegisterNewParticipantSuccessfully() {
        // Arrange
        when(eventRepository.findAllById(request.eventIds())).thenReturn(List.of(openEvent));
        when(participantRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(participantRepository.save(any(Participant.class))).thenAnswer(invocation -> {
            Participant participant = invocation.getArgument(0);
            participant.setId(PARTICIPANT_ID);
            return participant;
        });
        when(participantEventRepository.findAllByParticipantId(PARTICIPANT_ID)).thenReturn(List.of());

        // Act
        UUID result = registrationService.register(request);

        // Assert
        assertThat(result).isEqualTo(PARTICIPANT_ID);

        verify(eventRepository).findAllById(request.eventIds());
        verify(participantRepository).findByEmail(EMAIL);
        verify(participantRepository).save(participantCaptor.capture());
        verify(participantEventRepository).findAllByParticipantId(PARTICIPANT_ID);
        verify(participantEventRepository).saveAll(participantEventsCaptor.capture());
        verify(eventPublisher).publishEvent(registeredEventCaptor.capture());

        Participant savedParticipant = participantCaptor.getValue();
        assertThat(savedParticipant.getFirstName()).isEqualTo("Иван");
        assertThat(savedParticipant.getLastName()).isEqualTo("Иванов");
        assertThat(savedParticipant.getEmail()).isEqualTo(EMAIL);
        assertThat(savedParticipant.getCompany()).isEqualTo("ИнфоТеКС");
        assertThat(savedParticipant.getProjectRole()).isEqualTo("Разработчик");
        assertThat(savedParticipant.getStack()).isEqualTo("Java");
        assertThat(savedParticipant.getGrade()).isEqualTo("Middle");
        assertThat(savedParticipant.getTelegram()).isEqualTo("@ivan");
        assertThat(savedParticipant.isPersonalDataConsent()).isTrue();
        assertThat(savedParticipant.isPhotoConsent()).isTrue();
        assertThat(savedParticipant.isNewsletterConsent()).isFalse();

        List<ParticipantEvent> savedLinks = participantEventsCaptor.getValue();
        assertThat(savedLinks).hasSize(1);
        assertThat(savedLinks.getFirst().getParticipant().getId()).isEqualTo(PARTICIPANT_ID);
        assertThat(savedLinks.getFirst().getEvent().getId()).isEqualTo(EVENT_ID);
        assertThat(savedLinks.getFirst().getEvent().getTitle()).isEqualTo("Митап");

        ParticipantRegisteredEvent publishedEvent = registeredEventCaptor.getValue();
        assertThat(publishedEvent.participant().getId()).isEqualTo(PARTICIPANT_ID);
        assertThat(publishedEvent.request()).isSameAs(request);
        assertThat(publishedEvent.events()).containsExactly(openEvent);

        InOrder inOrder = inOrder(
                eventRepository,
                participantRepository,
                participantEventRepository,
                eventPublisher
        );
        inOrder.verify(eventRepository).findAllById(request.eventIds());
        inOrder.verify(participantRepository).findByEmail(EMAIL);
        inOrder.verify(participantRepository).save(any(Participant.class));
        inOrder.verify(participantEventRepository).findAllByParticipantId(PARTICIPANT_ID);
        inOrder.verify(participantEventRepository).saveAll(anyList());
        inOrder.verify(eventPublisher).publishEvent(any(ParticipantRegisteredEvent.class));

        verifyNoMoreInteractions(
                eventRepository,
                participantRepository,
                participantEventRepository,
                eventPublisher
        );
    }

    @Test
    @DisplayName("Мероприятие не найдено — регистрация останавливается")
    void shouldThrowWhenEventNotFound() {
        // Arrange
        when(eventRepository.findAllById(request.eventIds())).thenReturn(List.of());

        // Act / Assert
        assertThatThrownBy(() -> registrationService.register(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(EVENT_ID.toString());

        verify(eventRepository).findAllById(request.eventIds());
        verify(participantRepository, never()).save(any());
        verify(participantRepository, never()).findByEmail(any());
        verify(participantEventRepository, never()).saveAll(anyList());
        verify(participantEventRepository, never()).findAllByParticipantId(any());
        verifyNoInteractions(participantRepository, participantEventRepository, eventPublisher);
        verifyNoMoreInteractions(eventRepository);
    }

    @Test
    @DisplayName("Существующий участник обновляется, связь с мероприятием создаётся")
    void shouldUpdateExistingParticipant() {
        // Arrange
        Participant existing = Participant.builder()
                .id(PARTICIPANT_ID)
                .firstName("Старое")
                .lastName("Имя")
                .email(EMAIL)
                .company("Старая компания")
                .projectRole("Старая роль")
                .stack("Python")
                .grade("Junior")
                .telegram("@old")
                .personalDataConsent(false)
                .photoConsent(false)
                .newsletterConsent(true)
                .build();

        when(eventRepository.findAllById(request.eventIds())).thenReturn(List.of(openEvent));
        when(participantRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
        when(participantRepository.save(existing)).thenReturn(existing);
        when(participantEventRepository.findAllByParticipantId(PARTICIPANT_ID)).thenReturn(List.of());

        // Act
        UUID result = registrationService.register(request);

        // Assert
        assertThat(result).isEqualTo(PARTICIPANT_ID);

        verify(participantRepository).save(participantCaptor.capture());
        Participant savedParticipant = participantCaptor.getValue();
        assertThat(savedParticipant.getId()).isEqualTo(PARTICIPANT_ID);
        assertThat(savedParticipant.getFirstName()).isEqualTo("Иван");
        assertThat(savedParticipant.getLastName()).isEqualTo("Иванов");
        assertThat(savedParticipant.getEmail()).isEqualTo(EMAIL);
        assertThat(savedParticipant.getCompany()).isEqualTo("ИнфоТеКС");
        assertThat(savedParticipant.getProjectRole()).isEqualTo("Разработчик");
        assertThat(savedParticipant.getStack()).isEqualTo("Java");
        assertThat(savedParticipant.getGrade()).isEqualTo("Middle");
        assertThat(savedParticipant.getTelegram()).isEqualTo("@ivan");
        assertThat(savedParticipant.isPersonalDataConsent()).isTrue();
        assertThat(savedParticipant.isPhotoConsent()).isTrue();
        assertThat(savedParticipant.isNewsletterConsent()).isFalse();

        verify(participantEventRepository).saveAll(participantEventsCaptor.capture());
        List<ParticipantEvent> savedLinks = participantEventsCaptor.getValue();
        assertThat(savedLinks).hasSize(1);
        assertThat(savedLinks.getFirst().getParticipant()).isSameAs(existing);
        assertThat(savedLinks.getFirst().getEvent()).isSameAs(openEvent);

        verify(eventPublisher).publishEvent(registeredEventCaptor.capture());
        assertThat(registeredEventCaptor.getValue().participant()).isSameAs(existing);
        assertThat(registeredEventCaptor.getValue().events()).containsExactly(openEvent);

        verifyNoMoreInteractions(
                eventRepository,
                participantRepository,
                participantEventRepository,
                eventPublisher
        );
    }

    @Test
    @DisplayName("Повторная регистрация на то же мероприятие не создаёт дубликат связи")
    void shouldNotCreateDuplicateRegistration() {
        // Arrange
        Participant existing = Participant.builder()
                .id(PARTICIPANT_ID)
                .firstName("Иван")
                .lastName("Иванов")
                .email(EMAIL)
                .company("ИнфоТеКС")
                .projectRole("Разработчик")
                .stack("Java")
                .grade("Middle")
                .telegram("@ivan")
                .personalDataConsent(true)
                .photoConsent(true)
                .newsletterConsent(false)
                .build();

        ParticipantEvent existingLink = ParticipantEvent.builder()
                .id(UUID.fromString("44444444-4444-4444-4444-444444444444"))
                .participant(existing)
                .event(openEvent)
                .build();

        when(eventRepository.findAllById(request.eventIds())).thenReturn(List.of(openEvent));
        when(participantRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
        when(participantRepository.save(existing)).thenReturn(existing);
        when(participantEventRepository.findAllByParticipantId(PARTICIPANT_ID))
                .thenReturn(List.of(existingLink));

        // Act
        UUID result = registrationService.register(request);

        // Assert
        assertThat(result).isEqualTo(PARTICIPANT_ID);

        verify(participantRepository).save(existing);
        verify(participantEventRepository).findAllByParticipantId(PARTICIPANT_ID);
        verify(participantEventRepository, never()).saveAll(anyList());
        verify(eventPublisher).publishEvent(any(ParticipantRegisteredEvent.class));

        verifyNoMoreInteractions(
                eventRepository,
                participantRepository,
                participantEventRepository,
                eventPublisher
        );
    }

    @Test
    @DisplayName("Ошибка публикации события нотификации пробрасывается наружу")
    void shouldPropagateNotificationException() {
        // Arrange
        // В сервисе нотификация триггерится через ApplicationEventPublisher, а не прямым вызовом
        // RegistrationNotificationService (письмо отправляет listener после commit).
        RuntimeException notificationFailure = new RuntimeException("SMTP failed");

        when(eventRepository.findAllById(request.eventIds())).thenReturn(List.of(openEvent));
        when(participantRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(participantRepository.save(any(Participant.class))).thenAnswer(invocation -> {
            Participant participant = invocation.getArgument(0);
            participant.setId(PARTICIPANT_ID);
            return participant;
        });
        when(participantEventRepository.findAllByParticipantId(PARTICIPANT_ID)).thenReturn(List.of());
        doThrow(notificationFailure).when(eventPublisher).publishEvent(any(ParticipantRegisteredEvent.class));

        // Act / Assert
        assertThatThrownBy(() -> registrationService.register(request))
                .isSameAs(notificationFailure);

        InOrder inOrder = inOrder(
                eventRepository,
                participantRepository,
                participantEventRepository,
                eventPublisher
        );
        inOrder.verify(eventRepository).findAllById(request.eventIds());
        inOrder.verify(participantRepository).findByEmail(EMAIL);
        inOrder.verify(participantRepository).save(any(Participant.class));
        inOrder.verify(participantEventRepository).findAllByParticipantId(PARTICIPANT_ID);
        inOrder.verify(participantEventRepository).saveAll(anyList());
        inOrder.verify(eventPublisher).publishEvent(any(ParticipantRegisteredEvent.class));

        verifyNoMoreInteractions(
                eventRepository,
                participantRepository,
                participantEventRepository,
                eventPublisher
        );
    }

    @Test
    @DisplayName("При регистрации на новый event вместе с уже существующим — создаётся только недостающая связь")
    void shouldCreateOnlyMissingEventLinksForExistingParticipant() {
        // Arrange
        Event otherEvent = Event.builder()
                .id(OTHER_EVENT_ID)
                .title("Воркшоп")
                .eventDate(LocalDate.of(2026, 9, 1))
                .registrationEnabled(true)
                .build();

        Participant existing = Participant.builder()
                .id(PARTICIPANT_ID)
                .firstName("Иван")
                .lastName("Иванов")
                .email(EMAIL)
                .company("ИнфоТеКС")
                .projectRole("Разработчик")
                .stack("Java")
                .grade("Middle")
                .personalDataConsent(true)
                .photoConsent(true)
                .newsletterConsent(false)
                .build();

        ParticipantEvent existingLink = ParticipantEvent.builder()
                .participant(existing)
                .event(openEvent)
                .build();

        ParticipantRegistrationRequest multiEventRequest = new ParticipantRegistrationRequest(
                "Иван",
                "Иванов",
                EMAIL,
                "ИнфоТеКС",
                "Разработчик",
                "Java",
                "Middle",
                "@ivan",
                List.of(EVENT_ID, OTHER_EVENT_ID),
                true,
                true,
                false
        );

        when(eventRepository.findAllById(multiEventRequest.eventIds()))
                .thenReturn(List.of(openEvent, otherEvent));
        when(participantRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
        when(participantRepository.save(existing)).thenReturn(existing);
        when(participantEventRepository.findAllByParticipantId(PARTICIPANT_ID))
                .thenReturn(List.of(existingLink));

        // Act
        registrationService.register(multiEventRequest);

        // Assert
        verify(participantEventRepository).saveAll(participantEventsCaptor.capture());
        List<ParticipantEvent> savedLinks = participantEventsCaptor.getValue();
        assertThat(savedLinks).hasSize(1);
        assertThat(savedLinks.getFirst().getEvent().getId()).isEqualTo(OTHER_EVENT_ID);
        assertThat(savedLinks.getFirst().getParticipant().getId()).isEqualTo(PARTICIPANT_ID);
    }
}
