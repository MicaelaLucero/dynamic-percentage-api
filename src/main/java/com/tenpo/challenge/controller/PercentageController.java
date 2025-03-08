package com.tenpo.challenge.controller;

import com.tenpo.challenge.service.PercentageService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/percentage")
@RequiredArgsConstructor
@Validated
public class PercentageController {

    private final PercentageService percentageService;

    @GetMapping
    public ResponseEntity<Double> getPercentage() {
        return ResponseEntity.ok(percentageService.getPercentage());
    }

    @PutMapping
    public ResponseEntity<String> updatePercentage(@RequestParam @NotNull @Min(0) Double newPercentage) {
        return ResponseEntity.ok(percentageService.updatePercentage(newPercentage));
    }
}

