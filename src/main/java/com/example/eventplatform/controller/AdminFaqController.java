package com.example.eventplatform.controller;

import com.example.eventplatform.dto.FaqRequest;
import com.example.eventplatform.dto.FaqResponse;
import com.example.eventplatform.service.FaqAdminService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/faq")
@RequiredArgsConstructor
public class AdminFaqController {

    private final FaqAdminService faqAdminService;

    @GetMapping
    public List<FaqResponse> findAll() {
        return faqAdminService.findAll();
    }

    @GetMapping("/{id}")
    public FaqResponse findById(@PathVariable UUID id) {
        return faqAdminService.findById(id);
    }

    @PostMapping
    public ResponseEntity<FaqResponse> create(@Valid @RequestBody FaqRequest request) {
        FaqResponse created = faqAdminService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public FaqResponse update(@PathVariable UUID id, @Valid @RequestBody FaqRequest request) {
        return faqAdminService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        faqAdminService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
