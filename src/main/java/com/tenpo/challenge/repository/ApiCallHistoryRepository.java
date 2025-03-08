package com.tenpo.challenge.repository;

import com.tenpo.challenge.entity.ApiCallHistory;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiCallHistoryRepository extends JpaRepository<ApiCallHistory, Long> {
    @NonNull
    Page<ApiCallHistory> findAll(@NonNull Pageable pageable);
}