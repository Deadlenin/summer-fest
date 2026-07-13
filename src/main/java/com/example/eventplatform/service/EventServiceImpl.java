package com.example.eventplatform.service;

import com.example.eventplatform.dto.EventResponse;
import com.example.eventplatform.entity.Event;
import com.example.eventplatform.repository.EventRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getAvailableEvents() {
        return eventRepository.findByRegistrationEnabledTrueOrderByEventDateAscTitleAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private EventResponse toResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getEventDate(),
                event.getCompanyName(),
                event.getLocation()
        );
    }
}
