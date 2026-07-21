package com.tradingjournal.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradingjournal.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Thin client around an OpenAI-compatible "chat completions" endpoint.
 * Works with OpenAI directly, or any OpenAI-compatible provider (Groq, OpenRouter,
 * a local Ollama instance with the OpenAI-compatible API enabled, etc.) by changing
 * app.ai.base-url and app.ai.model in application.properties.
 */
@Component
@Slf4j
public class AiClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${app.ai.model:gpt-4o-mini}")
    private String model;

    @Value("${app.ai.enabled:true}")
    private boolean enabled;

    public String chat(String systemPrompt, String userPrompt) {
        if (!enabled) {
            throw new BadRequestException("AI features are disabled (app.ai.enabled=false).");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new BadRequestException(
                "AI features need an API key. Set app.ai.api-key in " +
                "backend/src/main/resources/application.properties (see README section on AI setup)."
            );
        }

        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    ),
                    "temperature", 0.6,
                    "max_tokens", 700
            );

            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                log.warn("AI provider returned {}: {}", response.statusCode(), response.body());
                throw new BadRequestException(
                    "The AI provider rejected the request (HTTP " + response.statusCode() + "). " +
                    "Double-check your app.ai.api-key and app.ai.model in application.properties."
                );
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode content = root.path("choices").path(0).path("message").path("content");

            if (content.isMissingNode() || content.asText().isBlank()) {
                throw new BadRequestException("The AI provider returned an empty response. Please try again.");
            }

            return content.asText().trim();

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI request failed", e);
            throw new BadRequestException("Could not reach the AI provider: " + e.getMessage());
        }
    }
}
