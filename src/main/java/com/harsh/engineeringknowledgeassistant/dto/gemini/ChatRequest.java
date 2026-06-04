package com.harsh.engineeringknowledgeassistant.dto.gemini;

public record ChatRequest( String question ) {
}

// Java automatically generate kar deta hai:
// question()
// equals()
// hashCode()
// toString()
// constructor