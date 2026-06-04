package com.harsh.engineeringknowledgeassistant.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.harsh.engineeringknowledgeassistant.dto.gemini.Document;
import com.harsh.engineeringknowledgeassistant.dto.gemini.DocumentChunk;

@Service
public class ChunkingService {

    public List<DocumentChunk> chunk( List<Document> documents ) {

        List<DocumentChunk> chunks =
                new ArrayList<>();

        for (Document document : documents) {

            String[] parts =
                    document.content().replace("\r\n", "\n")
                            .split("\\n\\n");

            for (String part : parts) {

                if (!part.isBlank()) {

                    chunks.add(
                            new DocumentChunk(
                                part,
                                new ArrayList<Double>(),
                                document.fileName()
                            )
                    );
                }
            }
        }

        return chunks;
    }
}