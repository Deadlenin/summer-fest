package com.example.eventplatform.service;

import com.example.eventplatform.dto.EventResponse;
import java.util.List;

public interface EventService {

    List<EventResponse> getAvailableEvents();
}
