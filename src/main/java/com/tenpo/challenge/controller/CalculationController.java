package com.tenpo.challenge.controller;

import com.tenpo.challenge.dto.CalculationRequest;
import com.tenpo.challenge.dto.CalculationResponse;
import com.tenpo.challenge.service.CalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/calculations")
@RequiredArgsConstructor
public class CalculationController {

    private final CalculationService calculationService;

    @PostMapping
    public ResponseEntity<CalculationResponse> calculate(@RequestBody CalculationRequest request) {
        return ResponseEntity.ok(calculationService.calculate(request));
    }
}
