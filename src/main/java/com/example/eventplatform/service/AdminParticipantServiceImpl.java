package com.example.eventplatform.service;

import com.example.eventplatform.repository.ParticipantEventRepository;
import com.example.eventplatform.repository.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminParticipantServiceImpl implements AdminParticipantService {

    private final ParticipantRepository participantRepository;
    private final ParticipantEventRepository participantEventRepository;

    @Override
    @Transactional
    public long deleteAllParticipants() {
        long count = participantRepository.count();
        participantEventRepository.deleteAllInBatch();
        participantRepository.deleteAllInBatch();
        return count;
    }
}
