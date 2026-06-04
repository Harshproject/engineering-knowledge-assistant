package com.harsh.engineeringknowledgeassistant.dto.gemini;

import java.util.List;

//for storing chunk as well as its embeddinngs
public record DocumentChunk( String text, List<Double> embedding, String source) {
} 