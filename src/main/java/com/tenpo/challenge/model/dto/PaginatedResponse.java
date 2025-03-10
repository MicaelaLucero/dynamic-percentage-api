package com.tenpo.challenge.model.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedResponse<T> {
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private List<T> data;
}