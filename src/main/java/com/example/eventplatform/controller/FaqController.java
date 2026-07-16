package com.example.eventplatform.controller;

import com.example.eventplatform.dto.FaqResponse;
import com.example.eventplatform.service.FaqService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/faq")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    @GetMapping
    public List<FaqResponse> getActiveFaqs() {
        return faqService.getActiveFaqs();
    }
}
