package com.example.eventplatform.repository;

import com.example.eventplatform.entity.Faq;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FaqRepository extends JpaRepository<Faq, UUID> {

    @Query("""
            SELECT f FROM Faq f
            WHERE f.active = true
            ORDER BY f.sortOrder ASC
            """)
    List<Faq> findActiveOrdered();

    @Query("""
            SELECT f FROM Faq f
            ORDER BY f.sortOrder ASC
            """)
    List<Faq> findAllOrdered();
}
