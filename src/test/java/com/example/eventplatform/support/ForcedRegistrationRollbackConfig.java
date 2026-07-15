package com.example.eventplatform.support;

import com.example.eventplatform.event.ParticipantRegisteredEvent;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Test-only listener that fails inside the same transaction (BEFORE_COMMIT),
 * so PostgreSQL rolls back Participant / ParticipantEvent writes.
 * Production still uses AFTER_COMMIT notification listener.
 */
@TestConfiguration
public class ForcedRegistrationRollbackConfig {

    @Bean
    ForcedRegistrationRollbackListener forcedRegistrationRollbackListener() {
        return new ForcedRegistrationRollbackListener();
    }

    static class ForcedRegistrationRollbackListener {

        @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
        public void failBeforeCommit(ParticipantRegisteredEvent event) {
            throw new IllegalStateException("Forced rollback for integration test");
        }
    }
}
