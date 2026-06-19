package com.xfl.aicareeragent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AgentChatRequest {

    @NotBlank(message = "问题不能为空")
    private String question;
}
