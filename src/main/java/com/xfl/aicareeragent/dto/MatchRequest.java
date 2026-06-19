package com.xfl.aicareeragent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MatchRequest {

    @NotBlank(message = "简历内容不能为空")
    private String resumeText;

    @NotBlank(message = "岗位描述不能为空")
    private String jobDescription;
}
