package com.xfl.aicareeragent.controller;

import com.xfl.aicareeragent.common.Result;
import com.xfl.aicareeragent.dto.MatchRequest;
import com.xfl.aicareeragent.dto.MatchResult;
import com.xfl.aicareeragent.entity.AnalysisRecordEntity;
import com.xfl.aicareeragent.service.AnalysisService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/match")
    public Result<MatchResult> match(@Valid @RequestBody MatchRequest request) {
        MatchResult result = analysisService.match(request);
        return Result.success(result);
    }

    @GetMapping("/history")
    public Result<List<AnalysisRecordEntity>> history() {
        List<AnalysisRecordEntity> records = analysisService.getHistory();
        return Result.success(records);
    }
}
