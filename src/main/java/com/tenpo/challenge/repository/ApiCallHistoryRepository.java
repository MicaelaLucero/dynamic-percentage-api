package com.tenpo.challenge.repository;

import com.tenpo.challenge.model.entity.ApiCallHistoryEntity;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiCallHistoryRepository extends JpaRepository<ApiCallHistoryEntity, Long> {
    @NonNull
    Page<ApiCallHistoryEntity> findAll(@NonNull Pageable pageable);
}