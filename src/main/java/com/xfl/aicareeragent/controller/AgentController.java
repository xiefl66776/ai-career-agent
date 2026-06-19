package com.xfl.aicareeragent.controller;

import com.xfl.aicareeragent.common.Result;
import com.xfl.aicareeragent.dto.AgentChatRequest;
import com.xfl.aicareeragent.service.AgentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/chat")
    public Result<String> chat(@Valid @RequestBody AgentChatRequest request) {
        String answer = agentService.chat(request.getQuestion());
        return Result.success(answer);
    }
}
