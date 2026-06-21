package com.harsh.engineeringknowledgeassistant.dto.gemini;

import java.util.List;

public record CachedChunk(
        String text,

        List<Double> embedding,

        String source
) {
    
}
