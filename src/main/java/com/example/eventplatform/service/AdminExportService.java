package com.example.eventplatform.service;

import java.util.List;
import java.util.UUID;

public interface AdminExportService {

    byte[] exportParticipantsToExcel(List<UUID> eventIds);
}
