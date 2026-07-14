package com.example.eventplatform.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "participants")
public class Participant {

    @Id
    @GeneratedValue
    @UuidGenerator
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private String company;

    @Column(name = "project_role", nullable = false)
    private String projectRole;

    @Column(nullable = false)
    private String stack;

    private String grade;

    private String telegram;

    @Column(name = "personal_data_consent", nullable = false)
    private boolean personalDataConsent;

    @Column(name = "photo_consent", nullable = false)
    private boolean photoConsent;

    /** Reserved for future marketing emails; registration confirmation is sent regardless. */
    @Column(name = "newsletter_consent", nullable = false)
    private boolean newsletterConsent;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = false)
    @Builder.Default
    private Set<ParticipantEvent> participantEvents = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
