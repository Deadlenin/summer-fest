package com.example.eventplatform.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.eventplatform.dto.EventRequest;
import com.example.eventplatform.dto.ParticipantRegistrationRequest;
import com.example.eventplatform.entity.Event;
import com.example.eventplatform.entity.Participant;
import com.example.eventplatform.support.AbstractIntegrationTest;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class AdminEventIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Создание мероприятия через admin API")
    void shouldCreateEvent() throws Exception {
        EventRequest request = sampleEventRequest("Новый митап");

        String response = mockMvc.perform(post("/api/admin/events")
                        .with(httpBasic(adminUsername(), adminPassword()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Новый митап"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.extendedDescription").value("Extended description"))
                .andExpect(jsonPath("$.location").value("Office"))
                .andExpect(jsonPath("$.registrationEnabled").value(true))
                .andExpect(jsonPath("$.sortOrder").value(10))
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID createdId = UUID.fromString(objectMapper.readTree(response).get("id").asText());
        Event saved = eventRepository.findById(createdId).orElseThrow();
        assertThat(saved.getTitle()).isEqualTo("Новый митап");
        assertThat(saved.getLocation()).isEqualTo("Office");
        assertThat(saved.getEventDate()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(eventRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Обновление мероприятия через admin API")
    void shouldUpdateEvent() throws Exception {
        Event existing = persistOpenEvent("Старое название");
        EventRequest updateRequest = new EventRequest(
                "Обновлённое название",
                "Новое описание",
                "Новое расширенное",
                LocalDate.of(2026, 10, 5),
                "СПб",
                false,
                5
        );

        mockMvc.perform(put("/api/admin/events/{id}", existing.getId())
                        .with(httpBasic(adminUsername(), adminPassword()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existing.getId().toString()))
                .andExpect(jsonPath("$.title").value("Обновлённое название"))
                .andExpect(jsonPath("$.description").value("Новое описание"))
                .andExpect(jsonPath("$.extendedDescription").value("Новое расширенное"))
                .andExpect(jsonPath("$.location").value("СПб"))
                .andExpect(jsonPath("$.registrationEnabled").value(false))
                .andExpect(jsonPath("$.sortOrder").value(5));

        Event updated = eventRepository.findById(existing.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("Обновлённое название");
        assertThat(updated.getDescription()).isEqualTo("Новое описание");
        assertThat(updated.getExtendedDescription()).isEqualTo("Новое расширенное");
        assertThat(updated.getLocation()).isEqualTo("СПб");
        assertThat(updated.getEventDate()).isEqualTo(LocalDate.of(2026, 10, 5));
        assertThat(updated.isRegistrationEnabled()).isFalse();
        assertThat(updated.getSortOrder()).isEqualTo(5);
    }

    @Test
    @DisplayName("Удаление мероприятия удаляет связи и оставляет участника")
    void shouldDeleteEventAndKeepParticipant() throws Exception {
        Event event = persistOpenEvent("К удалению");
        ParticipantRegistrationRequest registrationRequest =
                registrationRequest("keep-participant@mail.ru", event.getId());
        UUID participantId = registerParticipantSuccessfully(registrationRequest);

        assertThat(participantEventRepository.count()).isEqualTo(1);

        mockMvc.perform(delete("/api/admin/events/{id}", event.getId())
                        .with(httpBasic(adminUsername(), adminPassword())))
                .andExpect(status().isNoContent());

        assertThat(eventRepository.findById(event.getId())).isEmpty();
        assertThat(participantEventRepository.count()).isZero();

        Participant participant = participantRepository.findById(participantId).orElseThrow();
        assertThat(participant.getEmail()).isEqualTo("keep-participant@mail.ru");
        assertThat(participantRepository.count()).isEqualTo(1);
    }
}
