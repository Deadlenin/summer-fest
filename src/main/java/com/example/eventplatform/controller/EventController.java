package com.example.eventplatform.controller;

import com.example.eventplatform.dto.EventResponse;
import com.example.eventplatform.service.EventService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public List<EventResponse> getAvailableEvents() {
        return eventService.getAvailableEvents();
    }
}
