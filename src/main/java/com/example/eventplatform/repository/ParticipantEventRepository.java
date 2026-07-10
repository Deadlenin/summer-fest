package com.example.eventplatform.repository;

import com.example.eventplatform.entity.ParticipantEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ParticipantEventRepository extends JpaRepository<ParticipantEvent, UUID> {

    List<ParticipantEvent> findAllByParticipantId(UUID participantId);

    boolean existsByParticipantIdAndEventId(UUID participantId, UUID eventId);

    @Query("""
            select pe
            from ParticipantEvent pe
            join fetch pe.participant p
            join fetch pe.event e
            order by p.createdAt asc, e.title asc
            """)
    List<ParticipantEvent> findAllWithParticipantAndEvent();
}
