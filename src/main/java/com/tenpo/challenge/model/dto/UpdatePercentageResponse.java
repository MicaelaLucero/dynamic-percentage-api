package com.tenpo.challenge.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePercentageResponse {
    private Double newPercentage;
}