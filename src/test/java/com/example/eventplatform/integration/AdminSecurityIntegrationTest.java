package com.example.eventplatform.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.eventplatform.dto.EventRequest;
import com.example.eventplatform.entity.Event;
import com.example.eventplatform.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class AdminSecurityIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Admin API без Basic Auth возвращает 401")
    void shouldRejectAdminRequestsWithoutAuth() throws Exception {
        EventRequest request = sampleEventRequest("Secure event");
        Event existing = persistOpenEvent("Existing");

        mockMvc.perform(get("/api/admin/events"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/admin/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/admin/events/{id}", existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/admin/events/{id}", existing.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Admin API с неверными credentials возвращает 401")
    void shouldRejectAdminRequestsWithInvalidCredentials() throws Exception {
        EventRequest request = sampleEventRequest("Secure event");
        Event existing = persistOpenEvent("Existing");

        mockMvc.perform(get("/api/admin/events")
                        .with(httpBasic(adminUsername(), "wrong-password")))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/admin/events")
                        .with(httpBasic("wrong", adminPassword()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/admin/events/{id}", existing.getId())
                        .with(httpBasic(adminUsername(), "wrong-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/admin/events/{id}", existing.getId())
                        .with(httpBasic("wrong", "wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Admin API с корректными credentials разрешает 200/201/204")
    void shouldAllowAdminRequestsWithValidCredentials() throws Exception {
        EventRequest createRequest = sampleEventRequest("Allowed create");

        String createdResponse = mockMvc.perform(post("/api/admin/events")
                        .with(httpBasic(adminUsername(), adminPassword()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String createdId = objectMapper.readTree(createdResponse).get("id").asText();

        mockMvc.perform(get("/api/admin/events")
                        .with(httpBasic(adminUsername(), adminPassword())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/events/{id}", createdId)
                        .with(httpBasic(adminUsername(), adminPassword())))
                .andExpect(status().isOk());

        EventRequest updateRequest = sampleEventRequest("Allowed update");
        mockMvc.perform(put("/api/admin/events/{id}", createdId)
                        .with(httpBasic(adminUsername(), adminPassword()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/admin/events/{id}", createdId)
                        .with(httpBasic(adminUsername(), adminPassword())))
                .andExpect(status().isNoContent());
    }
}
