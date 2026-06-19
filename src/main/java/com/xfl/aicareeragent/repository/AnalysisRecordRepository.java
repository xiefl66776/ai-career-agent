package com.xfl.aicareeragent.repository;

import com.xfl.aicareeragent.entity.AnalysisRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalysisRecordRepository extends JpaRepository<AnalysisRecordEntity, Long> {

    List<AnalysisRecordEntity> findAllByOrderByCreatedAtDesc();
}
