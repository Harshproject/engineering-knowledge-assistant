package com.harsh.engineeringknowledgeassistant.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.harsh.engineeringknowledgeassistant.dto.gemini.DocumentChunk;

@Service
public class RagService {

    private final RetrievalService retrievalService;
    private final GeminiService geminiService;

    public RagService(
            RetrievalService retrievalService,
            GeminiService geminiService
    ) {
        this.retrievalService = retrievalService;
        this.geminiService = geminiService;
    }

    public String ask(String question) {

        List<DocumentChunk> chunks =
                retrievalService.retrieve(
                        question,
                        3
                );

        // prompt build karenge
        String context = chunks.stream()
        .map(DocumentChunk::text)
        .collect(Collectors.joining("\n\n"));

        String prompt = """
        You are an engineering knowledge assistant.

        Answer ONLY using the provided context.

        If the answer is not present in the context,
        say that the information is not available.

        Context:
        %s

        Question:
        %s
        """
        .formatted(context, question);
        System.out.print(prompt);
        return geminiService.askGemini(prompt);
        // return prompt;
    }
}