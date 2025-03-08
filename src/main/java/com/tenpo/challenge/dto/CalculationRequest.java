package com.tenpo.challenge.dto;

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
