package com.example.eventplatform.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.eventplatform.dto.EventRequest;
import com.example.eventplatform.dto.ParticipantRegistrationRequest;
import com.example.eventplatform.entity.Event;
import com.example.eventplatform.repository.EventRepository;
import com.example.eventplatform.repository.ParticipantEventRepository;
import com.example.eventplatform.repository.ParticipantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    /**
     * One shared PostgreSQL container for all IT classes.
     * Avoids Spring context / Hikari reconnecting to a stopped @Container per class.
     */
    static final PostgreSQLContainer<?> POSTGRES;

    static {
        POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("event_platform")
                .withUsername("test")
                .withPassword("test");
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected EventRepository eventRepository;

    @Autowired
    protected ParticipantRepository participantRepository;

    @Autowired
    protected ParticipantEventRepository participantEventRepository;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE participant_events, participants, events
                RESTART IDENTITY CASCADE
                """);
    }

    protected String adminUsername() {
        return ADMIN_USERNAME;
    }

    protected String adminPassword() {
        return ADMIN_PASSWORD;
    }

    protected Event persistOpenEvent(String title) {
        return eventRepository.save(Event.builder()
                .title(title)
                .description("Short description")
                .extendedDescription("Extended description")
                .eventDate(LocalDate.of(2026, 8, 15))
                .location("Moscow")
                .registrationEnabled(true)
                .sortOrder(1)
                .build());
    }

    protected EventRequest sampleEventRequest(String title) {
        return new EventRequest(
                title,
                "Description",
                "Extended description",
                LocalDate.of(2026, 9, 1),
                "Office",
                true,
                10
        );
    }

    protected ParticipantRegistrationRequest registrationRequest(String email, UUID... eventIds) {
        return new ParticipantRegistrationRequest(
                "Иван",
                "Иванов",
                email,
                "ИнфоТеКС",
                "Разработчик",
                "Java",
                "Middle",
                "@ivan",
                List.of(eventIds),
                true,
                true,
                false
        );
    }

    protected ResultActions registerParticipant(ParticipantRegistrationRequest request) throws Exception {
        return mockMvc.perform(post("/api/participants/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    protected UUID registerParticipantSuccessfully(ParticipantRegistrationRequest request) throws Exception {
        String response = registerParticipant(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(response).get("participantId").asText());
    }
}
