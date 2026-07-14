package com.example.eventplatform.service;

import com.example.eventplatform.dto.EventRequest;
import com.example.eventplatform.dto.EventResponse;
import java.util.List;
import java.util.UUID;

public interface EventAdminService {

    List<EventResponse> findAll();

    EventResponse findById(UUID id);

    EventResponse create(EventRequest request);

    EventResponse update(UUID id, EventRequest request);

    void delete(UUID id);
}
