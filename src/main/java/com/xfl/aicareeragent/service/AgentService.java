package com.xfl.aicareeragent.service;

import com.xfl.aicareeragent.entity.AnalysisRecordEntity;
import com.xfl.aicareeragent.repository.AnalysisRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AgentService {

    private final AnalysisRecordRepository analysisRecordRepository;
    private final DeepSeekService deepSeekService;

    public AgentService(AnalysisRecordRepository analysisRecordRepository,
                        DeepSeekService deepSeekService) {
        this.analysisRecordRepository = analysisRecordRepository;
        this.deepSeekService = deepSeekService;
    }

    public String chat(String question) {
        List<AnalysisRecordEntity> records = analysisRecordRepository.findAllByOrderByCreatedAtDesc();
        if (records.isEmpty()) {
            return "请先完成一次简历岗位匹配分析。";
        }

        AnalysisRecordEntity latest = records.get(0);
        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(latest, question);

        return deepSeekService.chat(systemPrompt, userPrompt);
    }

    private String buildSystemPrompt() {
        return """
                你是一名 AI 求职 Agent，专门帮助求职者准备 Java 后端实习和 AI 应用开发实习岗位。

                你的职责：
                1. 基于用户的简历、目标岗位 JD 和历史匹配分析结果，提供个性化的求职指导
                2. 回答关于 Java 后端实习、AI 应用开发实习、项目优化、学习路线的问题
                3. 回答必须具体、可执行，给出明确的操作步骤和参考资源方向
                4. 如果用户问学习路线，给出按天/周的具体计划
                5. 如果用户问项目优化，指出具体的技术方案和改进点
                6. 如果用户问面试准备，给出该岗位的高频考点和准备策略
                7. 回答风格：简洁直接，避免空泛的套话，每条建议都要能落地""";
    }

    private String buildUserPrompt(AnalysisRecordEntity record, String question) {
        return """
                以下是我的简历、岗位 JD 和历史匹配分析结果：

                【简历内容】
                %s

                【岗位 JD】
                %s

                【历史匹配分析结果】
                %s

                【我的问题】
                %s""".formatted(record.getResumeText(), record.getJobDescription(),
                record.getResultJson(), question);
    }
}
