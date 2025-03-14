package com.tenpo.challenge.model.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CalculationResponse {
    private double firstNumber;
    private double secondNumber;
    private double percentage;
    private double result;
}
