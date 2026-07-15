package com.example.eventplatform.service;

import com.example.eventplatform.dto.EventResponse;
import com.example.eventplatform.mapper.EventMapper;
import com.example.eventplatform.repository.EventRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getAvailableEvents() {
        return eventRepository.findAllOrdered().stream()
                .map(eventMapper::toResponse)
                .toList();
    }
}
