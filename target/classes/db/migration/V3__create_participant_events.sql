CREATE TABLE participant_events (
    id UUID PRIMARY KEY,
    participant_id UUID NOT NULL,
    event_id UUID NOT NULL,
    created_at TIMESTAMP,
    CONSTRAINT fk_participant_events_participant
        FOREIGN KEY (participant_id) REFERENCES participants (id),
    CONSTRAINT fk_participant_events_event
        FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT uk_participant_events_participant_event
        UNIQUE (participant_id, event_id)
);
