package com.example.eventplatform.service;

import com.example.eventplatform.dto.FaqResponse;
import com.example.eventplatform.mapper.FaqMapper;
import com.example.eventplatform.repository.FaqRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FaqServiceImpl implements FaqService {

    private final FaqRepository faqRepository;
    private final FaqMapper faqMapper;

    @Override
    @Transactional(readOnly = true)
    public List<FaqResponse> getActiveFaqs() {
        return faqRepository.findActiveOrdered().stream()
                .map(faqMapper::toResponse)
                .toList();
    }
}
