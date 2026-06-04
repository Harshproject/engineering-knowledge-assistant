package com.harsh.engineeringknowledgeassistant.dto.gemini;

import java.util.List;

public record GeminiRequest(
        List<Content> contents
) {
}