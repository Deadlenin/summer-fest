package com.example.eventplatform.service;

import com.example.eventplatform.dto.FaqRequest;
import com.example.eventplatform.dto.FaqResponse;
import java.util.List;
import java.util.UUID;

public interface FaqAdminService {

    List<FaqResponse> findAll();

    FaqResponse findById(UUID id);

    FaqResponse create(FaqRequest request);

    FaqResponse update(UUID id, FaqRequest request);

    void delete(UUID id);
}
