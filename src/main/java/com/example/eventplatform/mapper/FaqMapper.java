package com.example.eventplatform.mapper;

import com.example.eventplatform.dto.FaqRequest;
import com.example.eventplatform.dto.FaqResponse;
import com.example.eventplatform.entity.Faq;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FaqMapper {

    @Mapping(source = "active", target = "isActive")
    FaqResponse toResponse(Faq faq);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "isActive", target = "active")
    Faq toEntity(FaqRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "isActive", target = "active")
    void updateEntity(@MappingTarget Faq faq, FaqRequest request);
}
