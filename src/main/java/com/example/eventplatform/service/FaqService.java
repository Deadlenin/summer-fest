package com.example.eventplatform.service;

import com.example.eventplatform.dto.FaqResponse;
import java.util.List;

public interface FaqService {

    List<FaqResponse> getActiveFaqs();
}
