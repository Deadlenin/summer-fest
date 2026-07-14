package com.example.eventplatform.service;

import com.example.eventplatform.entity.Participant;
import com.example.eventplatform.entity.ParticipantEvent;
import com.example.eventplatform.repository.ParticipantEventRepository;
import com.example.eventplatform.repository.ParticipantRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminExportServiceImpl implements AdminExportService {

    private static final String SHEET_NAME = "Participants";
    private static final String[] HEADERS = {
            "Фамилия",
            "Имя",
            "Компания",
            "Роль",
            "Стек",
            "Грейд",
            "Email",
            "Telegram",
            "Согласие на обработку персональных данных",
            "Согласие на фото/видеосъемку",
            "Согласие на рассылку",
            "Выбранные мероприятия",
            "Дата регистрации"
    };
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ParticipantRepository participantRepository;
    private final ParticipantEventRepository participantEventRepository;

    @Override
    @Transactional(readOnly = true)
    public byte[] exportParticipantsToExcel(List<UUID> eventIds) {
        List<Participant> participants = (eventIds == null || eventIds.isEmpty())
                ? participantRepository.findAll()
                : participantRepository.findDistinctByParticipantEventsEventIdInOrderByCreatedAtAsc(eventIds);
        Map<UUID, List<String>> eventTitlesByParticipantId = loadEventTitlesByParticipantId();

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(SHEET_NAME);
            createHeaderRow(sheet);
            fillDataRows(sheet, participants, eventTitlesByParticipantId);

            for (int columnIndex = 0; columnIndex < HEADERS.length; columnIndex++) {
                sheet.autoSizeColumn(columnIndex);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to generate Excel export", ex);
        }
    }

    private Map<UUID, List<String>> loadEventTitlesByParticipantId() {
        Map<UUID, List<String>> eventTitlesByParticipantId = new LinkedHashMap<>();

        for (ParticipantEvent participantEvent : participantEventRepository.findAllWithParticipantAndEvent()) {
            UUID participantId = participantEvent.getParticipant().getId();
            eventTitlesByParticipantId
                    .computeIfAbsent(participantId, ignored -> new ArrayList<>())
                    .add(participantEvent.getEvent().getTitle());
        }

        return eventTitlesByParticipantId;
    }

    private void createHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        for (int columnIndex = 0; columnIndex < HEADERS.length; columnIndex++) {
            Cell cell = headerRow.createCell(columnIndex);
            cell.setCellValue(HEADERS[columnIndex]);
        }
    }

    private void fillDataRows(Sheet sheet, List<Participant> participants, Map<UUID, List<String>> eventTitlesByParticipantId) {
        int rowIndex = 1;
        for (Participant participant : participants) {
            Row row = sheet.createRow(rowIndex++);
            List<String> eventTitles = eventTitlesByParticipantId.getOrDefault(participant.getId(), List.of());

            row.createCell(0).setCellValue(nullSafe(participant.getLastName()));
            row.createCell(1).setCellValue(nullSafe(participant.getFirstName()));
            row.createCell(2).setCellValue(nullSafe(participant.getCompany()));
            row.createCell(3).setCellValue(nullSafe(participant.getProjectRole()));
            row.createCell(4).setCellValue(nullSafe(participant.getStack()));
            row.createCell(5).setCellValue(nullSafe(participant.getGrade()));
            row.createCell(6).setCellValue(nullSafe(participant.getEmail()));
            row.createCell(7).setCellValue(nullSafe(participant.getTelegram()));
            row.createCell(8).setCellValue(yesNo(participant.isPersonalDataConsent()));
            row.createCell(9).setCellValue(yesNo(participant.isPhotoConsent()));
            row.createCell(10).setCellValue(yesNo(participant.isNewsletterConsent()));
            row.createCell(11).setCellValue(String.join(", ", eventTitles));
            row.createCell(12).setCellValue(
                    participant.getCreatedAt() == null
                            ? ""
                            : DATE_TIME_FORMATTER.format(participant.getCreatedAt())
            );
        }
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private String yesNo(boolean value) {
        return value ? "Да" : "Нет";
    }
}
