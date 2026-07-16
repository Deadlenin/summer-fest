package com.example.eventplatform.service;

import com.example.eventplatform.aspect.LogFaqWrite;
import com.example.eventplatform.dto.FaqRequest;
import com.example.eventplatform.dto.FaqResponse;
import com.example.eventplatform.entity.Faq;
import com.example.eventplatform.exception.ResourceNotFoundException;
import com.example.eventplatform.mapper.FaqMapper;
import com.example.eventplatform.repository.FaqRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FaqAdminServiceImpl implements FaqAdminService {

    private final FaqRepository faqRepository;
    private final FaqMapper faqMapper;

    @Override
    @Transactional(readOnly = true)
    public List<FaqResponse> findAll() {
        return faqRepository.findAllOrdered().stream()
                .map(faqMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FaqResponse findById(UUID id) {
        return faqMapper.toResponse(getFaqOrThrow(id));
    }

    @Override
    @Transactional
    @LogFaqWrite
    public FaqResponse create(FaqRequest request) {
        Faq faq = faqMapper.toEntity(request);
        Faq saved = faqRepository.save(faq);
        return faqMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @LogFaqWrite
    public FaqResponse update(UUID id, FaqRequest request) {
        Faq faq = getFaqOrThrow(id);
        faqMapper.updateEntity(faq, request);
        Faq saved = faqRepository.save(faq);
        return faqMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @LogFaqWrite
    public void delete(UUID id) {
        if (!faqRepository.existsById(id)) {
            throw new ResourceNotFoundException("FAQ not found: " + id);
        }
        faqRepository.deleteById(id);
    }

    private Faq getFaqOrThrow(UUID id) {
        return faqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FAQ not found: " + id));
    }
}
