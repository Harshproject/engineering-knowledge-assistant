package com.harsh.engineeringknowledgeassistant.service;

import java.util.List;

import com.harsh.engineeringknowledgeassistant.dto.gemini.Document;

public interface DocumentSource {
    List<Document> loadDocuments();
}
