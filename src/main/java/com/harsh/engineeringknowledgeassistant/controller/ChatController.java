package com.harsh.engineeringknowledgeassistant.controller;

import com.harsh.engineeringknowledgeassistant.dto.gemini.ChatRequest;
import com.harsh.engineeringknowledgeassistant.dto.gemini.Document;
import com.harsh.engineeringknowledgeassistant.dto.gemini.DocumentChunk;
import com.harsh.engineeringknowledgeassistant.dto.gemini.EmbeddingRequest;
import com.harsh.engineeringknowledgeassistant.service.ChunkingService;
import com.harsh.engineeringknowledgeassistant.service.ConfluenceDocumentSource;
import com.harsh.engineeringknowledgeassistant.service.DocumentLoaderService;
import com.harsh.engineeringknowledgeassistant.service.DocumentSource;
import com.harsh.engineeringknowledgeassistant.service.EmbeddingCacheService;
import com.harsh.engineeringknowledgeassistant.service.GeminiService;
import com.harsh.engineeringknowledgeassistant.service.RagService;
import com.harsh.engineeringknowledgeassistant.service.RetrievalService;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final GeminiService geminiService;
    private final RetrievalService retrievalService;
    private final RagService ragService;
    private final DocumentLoaderService documentLoaderService;
    private final ChunkingService chunkingService;
    private final ConfluenceDocumentSource confluenceDocumentSource;
    private final List<DocumentSource> sources;
    private final EmbeddingCacheService cacheService;

    public ChatController(GeminiService geminiService, RetrievalService retrievalService, RagService ragService, DocumentLoaderService documentLoaderService, ChunkingService chunkingService, ConfluenceDocumentSource confluenceDocumentSource, List<DocumentSource> sources, EmbeddingCacheService cacheService) {
        this.geminiService = geminiService;
        this.retrievalService=retrievalService;
        this.ragService=ragService;
        this.documentLoaderService=documentLoaderService;
        this.chunkingService=chunkingService;
        this.confluenceDocumentSource=confluenceDocumentSource;
        this.sources=sources;
        this.cacheService=cacheService;
    }

    @PostMapping("/ask")
    public String ask(@RequestBody ChatRequest request) {
        return geminiService.askGemini(
                request.question()
        );
    }

    @PostMapping("/embedding")
    public List<Double> embedding( @RequestBody EmbeddingRequest request) {
        return retrievalService.generateEmbedding(
                request.text()
        );
    }

    @GetMapping("/models")
    public String models() {
        return geminiService.listModels();
    }

    @PostMapping("/ragservice")
    public String ragService( @RequestBody EmbeddingRequest request) {
        return ragService.ask(
                request.text()
        );
    }

    @GetMapping("/docs")
    public List<Document> docs() {
        return documentLoaderService.loadDocuments();
    }

    @GetMapping("/chunks")
    public List<DocumentChunk> chunks() {

        return chunkingService.chunk(
                documentLoaderService.loadDocuments()
        );
    }

    @GetMapping("/documents")
    public List<Document> confluenceDocs(){

            List<Document> documents =
                    sources.stream()
                    .flatMap(
                            source->
                                    source.loadDocuments()
                                            .stream()
                    )
                    .toList();

            return documents;
    }

    @PostMapping("/cache-refresh")
    public String refreshCache() {
        try {
            cacheService.refresh();

            retrievalService.init();
            return "Cache refreshed";
        } catch (Exception e) {
            System.err.println("Error refreshing cache: " + e.getMessage());
            return "Error refreshing cache";
        }
        
    }
}