package com.example.eventplatform.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.eventplatform.dto.ParticipantRegistrationRequest;
import com.example.eventplatform.entity.Event;
import com.example.eventplatform.support.AbstractIntegrationTest;
import com.example.eventplatform.support.ForcedRegistrationRollbackConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

@Import(ForcedRegistrationRollbackConfig.class)
class RegistrationTransactionRollbackIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Ошибка до commit откатывает Participant и ParticipantEvent в PostgreSQL")
    void shouldRollbackParticipantAndLinksWhenTransactionFailsBeforeCommit() throws Exception {
        Event event = persistOpenEvent("Rollback event");
        ParticipantRegistrationRequest request = registrationRequest("rollback@mail.ru", event.getId());

        registerParticipant(request)
                .andExpect(status().isInternalServerError());

        assertThat(participantRepository.count()).isZero();
        assertThat(participantEventRepository.count()).isZero();
        assertThat(eventRepository.count()).isEqualTo(1);
        assertThat(participantRepository.findByEmail("rollback@mail.ru")).isEmpty();
    }
}
