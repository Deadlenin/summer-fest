package com.example.eventplatform.service;

import com.example.eventplatform.dto.EventRequest;
import com.example.eventplatform.dto.EventResponse;
import com.example.eventplatform.entity.Event;
import com.example.eventplatform.exception.ResourceNotFoundException;
import com.example.eventplatform.mapper.EventMapper;
import com.example.eventplatform.repository.EventRepository;
import com.example.eventplatform.repository.ParticipantEventRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventAdminServiceImpl implements EventAdminService {

    private final EventRepository eventRepository;
    private final ParticipantEventRepository participantEventRepository;
    private final EventMapper eventMapper;

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> findAll() {
        return eventRepository.findAllOrdered().stream()
                .map(eventMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse findById(UUID id) {
        return eventMapper.toResponse(getEventOrThrow(id));
    }

    @Override
    @Transactional
    public EventResponse create(EventRequest request) {
        Event event = eventMapper.toEntity(request);
        Event saved = eventRepository.save(event);
        return eventMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public EventResponse update(UUID id, EventRequest request) {
        Event event = getEventOrThrow(id);
        eventMapper.updateEntity(event, request);
        Event saved = eventRepository.save(event);
        return eventMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event not found: " + id);
        }
        participantEventRepository.deleteAllByEventId(id);
        eventRepository.deleteById(id);
    }

    private Event getEventOrThrow(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + id));
    }
}
