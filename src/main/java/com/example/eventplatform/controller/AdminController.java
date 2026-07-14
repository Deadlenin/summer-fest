package com.example.eventplatform.controller;

import com.example.eventplatform.service.AdminExportService;
import com.example.eventplatform.service.AdminParticipantService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final AdminExportService adminExportService;
    private final AdminParticipantService adminParticipantService;

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportParticipants(
            @RequestParam(required = false) List<UUID> eventIds
    ) {
        byte[] file = adminExportService.exportParticipantsToExcel(eventIds);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(EXCEL_CONTENT_TYPE))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("participants-export.xlsx")
                        .build()
                        .toString())
                .body(file);
    }

    @DeleteMapping("/participants")
    public ResponseEntity<Map<String, Long>> deleteAllParticipants() {
        long deletedCount = adminParticipantService.deleteAllParticipants();
        return ResponseEntity.ok(Map.of("deletedCount", deletedCount));
    }
}
