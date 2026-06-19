package com.xfl.aicareeragent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xfl.aicareeragent.dto.MatchRequest;
import com.xfl.aicareeragent.dto.MatchResult;
import com.xfl.aicareeragent.entity.AnalysisRecordEntity;
import com.xfl.aicareeragent.exception.BusinessException;
import com.xfl.aicareeragent.repository.AnalysisRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class AnalysisService {

    private final DeepSeekService deepSeekService;
    private final ObjectMapper objectMapper;
    private final AnalysisRecordRepository analysisRecordRepository;

    public AnalysisService(DeepSeekService deepSeekService,
                           ObjectMapper objectMapper,
                           AnalysisRecordRepository analysisRecordRepository) {
        this.deepSeekService = deepSeekService;
        this.objectMapper = objectMapper;
        this.analysisRecordRepository = analysisRecordRepository;
    }

    public MatchResult match(MatchRequest request) {
        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(request.getResumeText(), request.getJobDescription());

        String rawJson = deepSeekService.chatForJson(systemPrompt, userPrompt);
        String cleaned = cleanJson(rawJson);
        log.info("cleaned json length: {}", cleaned.length());

        try {
            MatchResult result = objectMapper.readValue(cleaned, MatchResult.class);
            saveRecord(request, result, cleaned);
            return result;
        } catch (JsonProcessingException e) {
            log.error("failed to parse match result, raw: {}", cleaned, e);
            throw new BusinessException("AI 返回结果解析失败，请重试");
        }
    }

    public List<AnalysisRecordEntity> getHistory() {
        return analysisRecordRepository.findAllByOrderByCreatedAtDesc();
    }

    private void saveRecord(MatchRequest request, MatchResult result, String resultJson) {
        AnalysisRecordEntity entity = new AnalysisRecordEntity();
        entity.setResumeText(request.getResumeText());
        entity.setJobDescription(request.getJobDescription());
        entity.setMatchScore(result.getMatchScore());
        entity.setResultJson(resultJson);
        entity.setCreatedAt(LocalDateTime.now());
        analysisRecordRepository.save(entity);
        log.info("analysis record saved, id={}", entity.getId());
    }

    private String buildSystemPrompt() {
        return """
                你是一位资深的职业规划专家和招聘顾问，擅长分析求职者简历与目标岗位的匹配度。

                你的任务是：
                1. 根据用户提供的简历内容和岗位描述，全面分析匹配程度
                2. matchScore 为匹配度评分（0-100），综合考虑技能、经验、项目经历、学历等因素
                3. matchedSkills 列出简历中与岗位要求匹配的技能
                4. missingSkills 列出岗位要求但简历中缺失的技能
                5. projectSuggestions 给出可以补充的项目建议，弥补技能缺口
                6. learningPlan 给出具体的学习计划，每天一个学习目标
                7. resumeSuggestions 给出简历优化建议，包括措辞、结构、亮点突出等方面
                8. summary 为整体评估总结，2-3句话

                请严格按照要求的 JSON 格式输出，不要有任何额外内容。""";
    }

    private String buildUserPrompt(String resumeText, String jobDescription) {
        return "简历内容：\n" + resumeText + "\n\n岗位描述：\n" + jobDescription;
    }

    private String cleanJson(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BusinessException("AI 返回内容为空");
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }
}
