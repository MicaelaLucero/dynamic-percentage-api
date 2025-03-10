package com.tenpo.challenge.controller;

import com.tenpo.challenge.model.dto.ApiCallHistoryResponse;
import com.tenpo.challenge.model.dto.PaginatedResponse;
import com.tenpo.challenge.model.entity.ApiCallHistoryEntity;
import com.tenpo.challenge.service.ApiCallHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
public class ApiCallHistoryController {

    private final ApiCallHistoryService apiCallHistoryService;

    @GetMapping
    public ResponseEntity<PaginatedResponse<ApiCallHistoryResponse>> getHistory(Pageable pageable) {
        return ResponseEntity.ok(apiCallHistoryService.getHistory(pageable));
    }
}
