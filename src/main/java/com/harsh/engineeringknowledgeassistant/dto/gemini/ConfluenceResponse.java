package com.harsh.engineeringknowledgeassistant.dto.gemini;

import java.util.List;

public record ConfluenceResponse(
    List<ConfluencePage> results
) {
    
}
