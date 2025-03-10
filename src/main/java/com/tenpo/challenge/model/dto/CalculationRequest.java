package com.tenpo.challenge.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculationRequest {
    private double firstNumber;
    private double secondNumber;
}
