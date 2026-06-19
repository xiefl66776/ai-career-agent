package com.xfl.aicareeragent.dto;

import lombok.Data;

import java.util.List;

@Data
public class MatchResult {

    private Integer matchScore;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private List<String> projectSuggestions;
    private List<String> learningPlan;
    private List<String> resumeSuggestions;
    private String summary;
}
