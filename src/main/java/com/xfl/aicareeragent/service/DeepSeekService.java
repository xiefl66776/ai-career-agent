package com.xfl.aicareeragent.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xfl.aicareeragent.config.DeepSeekProperties;
import com.xfl.aicareeragent.exception.BusinessException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
public class DeepSeekService {

    private final WebClient deepSeekWebClient;
    private final DeepSeekProperties properties;

    public DeepSeekService(WebClient deepSeekWebClient, DeepSeekProperties properties) {
        this.deepSeekWebClient = deepSeekWebClient;
        this.properties = properties;
    }

    public String chat(String systemPrompt, String userPrompt) {
        return doChat(systemPrompt, userPrompt, null);
    }

    public String chatForJson(String systemPrompt, String userPrompt) {
        String jsonSystemPrompt = systemPrompt + "\n\n请只返回 JSON 格式的数据，不要包含任何其他文字说明、代码块标记或注释。";
        return doChat(jsonSystemPrompt, userPrompt, "json_object");
    }

    private String doChat(String systemPrompt, String userPrompt, String responseFormat) {
        ChatRequest request = new ChatRequest();
        request.setModel(properties.getModel());
        request.setMessages(List.of(
                new Message("system", systemPrompt),
                new Message("user", userPrompt)
        ));
        if (responseFormat != null) {
            request.setResponseFormat(new ResponseFormat(responseFormat));
        }

        ChatResponse response = deepSeekWebClient.post()
                .uri("/v1/chat/completions")
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new BusinessException(
                                        clientResponse.statusCode().value(),
                                        "DeepSeek API 调用失败: " + body))))
                .bodyToMono(ChatResponse.class)
                .block();

        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new BusinessException("DeepSeek API 返回为空");
        }

        String content = response.getChoices().get(0).getMessage().getContent();
        log.info("DeepSeek tokens used: prompt={}, completion={}, total={}",
                response.getUsage().getPromptTokens(),
                response.getUsage().getCompletionTokens(),
                response.getUsage().getTotalTokens());
        return content;
    }

    @Data
    private static class ChatRequest {
        private String model;
        private List<Message> messages;
        @JsonProperty("response_format")
        private ResponseFormat responseFormat;
    }

    @Data
    private static class Message {
        private final String role;
        private final String content;
    }

    @Data
    private static class ResponseFormat {
        private final String type;
    }

    @Data
    private static class ChatResponse {
        private List<Choice> choices;
        private Usage usage;
    }

    @Data
    private static class Choice {
        private Message message;
    }

    @Data
    private static class Usage {
        @JsonProperty("prompt_tokens")
        private int promptTokens;
        @JsonProperty("completion_tokens")
        private int completionTokens;
        @JsonProperty("total_tokens")
        private int totalTokens;
    }
}
