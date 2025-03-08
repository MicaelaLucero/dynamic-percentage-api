package com.tenpo.challenge.controller;

import com.tenpo.challenge.dto.PaginatedResponse;
import com.tenpo.challenge.entity.ApiCallHistory;
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
    public ResponseEntity<PaginatedResponse<ApiCallHistory>> getHistory(Pageable pageable) {
        return ResponseEntity.ok(apiCallHistoryService.getHistory(pageable));
    }
}
