package com.example.eventplatform.repository;

import com.example.eventplatform.entity.Participant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<Participant, UUID> {

    Optional<Participant> findByEmail(String email);

    List<Participant> findDistinctByParticipantEventsEventIdInOrderByCreatedAtAsc(Collection<UUID> eventIds);
}
