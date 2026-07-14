package com.example.eventplatform.repository;

import com.example.eventplatform.entity.Event;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EventRepository extends JpaRepository<Event, UUID> {

    @Query("""
            SELECT e FROM Event e
            ORDER BY CASE WHEN e.sortOrder IS NULL THEN 1 ELSE 0 END,
                     e.sortOrder ASC,
                     e.eventDate ASC
            """)
    List<Event> findAllOrdered();

    @Query("""
            SELECT e from Event e
            WHERE e.registrationEnabled = true
            ORDER BY CASE WHEN e.sortOrder IS NULL THEN 1 ELSE 0 END,
                     e.sortOrder ASC,
                     e.eventDate ASC
            """)
    List<Event> findAvailableOrdered();
}
