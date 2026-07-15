package com.example.eventplatform.mapper;

import com.example.eventplatform.dto.EventRequest;
import com.example.eventplatform.dto.EventResponse;
import com.example.eventplatform.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EventMapper {

    EventResponse toResponse(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyName", ignore = true)
    @Mapping(target = "participantEvents", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Event toEntity(EventRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyName", ignore = true)
    @Mapping(target = "participantEvents", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Event event, EventRequest request);
}
