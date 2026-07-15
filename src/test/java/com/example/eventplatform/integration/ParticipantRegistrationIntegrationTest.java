package com.example.eventplatform.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.eventplatform.dto.ParticipantRegistrationRequest;
import com.example.eventplatform.entity.Event;
import com.example.eventplatform.entity.Participant;
import com.example.eventplatform.support.AbstractIntegrationTest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ParticipantRegistrationIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Регистрация нового участника через REST сохраняет Participant и ParticipantEvent")
    void shouldRegisterNewParticipant() throws Exception {
        Event event = persistOpenEvent("Митап");
        String originalTitle = event.getTitle();
        String originalDescription = event.getDescription();
        boolean originalRegistrationEnabled = event.isRegistrationEnabled();

        ParticipantRegistrationRequest request = registrationRequest("user@mail.ru", event.getId());

        UUID participantId = registerParticipantSuccessfully(request);

        Participant participant = participantRepository.findById(participantId).orElseThrow();
        assertThat(participant.getFirstName()).isEqualTo("Иван");
        assertThat(participant.getLastName()).isEqualTo("Иванов");
        assertThat(participant.getEmail()).isEqualTo("user@mail.ru");
        assertThat(participant.getCompany()).isEqualTo("ИнфоТеКС");
        assertThat(participant.getProjectRole()).isEqualTo("Разработчик");
        assertThat(participant.getStack()).isEqualTo("Java");
        assertThat(participant.getGrade()).isEqualTo("Middle");
        assertThat(participant.getTelegram()).isEqualTo("@ivan");
        assertThat(participant.isPersonalDataConsent()).isTrue();
        assertThat(participant.isPhotoConsent()).isTrue();
        assertThat(participant.isNewsletterConsent()).isFalse();

        assertThat(participantEventRepository.count()).isEqualTo(1);
        assertThat(participantEventRepository.existsByParticipantIdAndEventId(participantId, event.getId())).isTrue();

        Event eventAfter = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(eventAfter.getTitle()).isEqualTo(originalTitle);
        assertThat(eventAfter.getDescription()).isEqualTo(originalDescription);
        assertThat(eventAfter.isRegistrationEnabled()).isEqualTo(originalRegistrationEnabled);
        assertThat(eventRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Регистрация на несуществующее мероприятие возвращает 404 и ничего не сохраняет")
    void shouldReturnNotFoundWhenEventDoesNotExist() throws Exception {
        UUID missingEventId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        ParticipantRegistrationRequest request = registrationRequest("missing-event@mail.ru", missingEventId);

        registerParticipant(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Events not found: [" + missingEventId + "]"));

        assertThat(participantRepository.count()).isZero();
        assertThat(participantEventRepository.count()).isZero();
    }

    @Test
    @DisplayName("Повторная регистрация на то же мероприятие не создаёт дубликаты связей")
    void shouldNotCreateDuplicateRegistration() throws Exception {
        Event event = persistOpenEvent("Митап");
        ParticipantRegistrationRequest firstRequest = registrationRequest("repeat@mail.ru", event.getId());
        ParticipantRegistrationRequest secondRequest = new ParticipantRegistrationRequest(
                "Пётр",
                "Петров",
                "repeat@mail.ru",
                "Газпром",
                "Аналитик",
                "SQL",
                "Senior",
                "@petr",
                List.of(event.getId()),
                true,
                false,
                true
        );

        UUID firstId = registerParticipantSuccessfully(firstRequest);
        UUID secondId = registerParticipantSuccessfully(secondRequest);

        assertThat(secondId).isEqualTo(firstId);
        assertThat(participantRepository.count()).isEqualTo(1);
        assertThat(participantEventRepository.count()).isEqualTo(1);
        assertThat(participantEventRepository.existsByParticipantIdAndEventId(firstId, event.getId())).isTrue();

        Participant updated = participantRepository.findById(firstId).orElseThrow();
        assertThat(updated.getFirstName()).isEqualTo("Пётр");
        assertThat(updated.getLastName()).isEqualTo("Петров");
        assertThat(updated.getCompany()).isEqualTo("Газпром");
        assertThat(updated.getProjectRole()).isEqualTo("Аналитик");
        assertThat(updated.getStack()).isEqualTo("SQL");
        assertThat(updated.getGrade()).isEqualTo("Senior");
        assertThat(updated.getTelegram()).isEqualTo("@petr");
        assertThat(updated.isPhotoConsent()).isFalse();
        assertThat(updated.isNewsletterConsent()).isTrue();
    }
}
