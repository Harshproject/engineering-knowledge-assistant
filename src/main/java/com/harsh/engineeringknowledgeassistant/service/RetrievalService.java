package com.harsh.engineeringknowledgeassistant.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.harsh.engineeringknowledgeassistant.dto.gemini.Document;
import com.harsh.engineeringknowledgeassistant.dto.gemini.DocumentChunk;
import com.harsh.engineeringknowledgeassistant.dto.gemini.ScoreChunk;

import jakarta.annotation.PostConstruct;

@Service
public class RetrievalService {
    
        private final WebClient webClient;
        private final ObjectMapper objectMapper = new ObjectMapper();
        private final SimilarityService similarityService;
        private final List<DocumentChunk> chunks = new ArrayList<>();
        private final DocumentLoaderService documentLoaderService;
        private final ChunkingService chunkingService;

        @Value("${gemini.api.key}")
        private String apiKey;

        // @PostConstruct
        // public void init() {

        // chunks.add(
        //         new DocumentChunk(
        //                 "Payment Service processes transactions",
        //                 generateEmbedding(
        //                         "Payment Service processes transactions"
        //                 ),
        //                 "something.md"
        //         )
        // );

        // chunks.add(
        //         new DocumentChunk(
        //                 "User Service manages user accounts",
        //                 generateEmbedding(
        //                         "User Service manages user accounts"
        //                 ),
        //                 "something.md"
        //         )
        // );

        // chunks.add(
        //         new DocumentChunk(
        //                 "Inventory Service tracks stock",
        //                 generateEmbedding(
        //                         "Inventory Service tracks stock"
        //                 ),
        //                 "something.md"
        //         )
        // );
        // }

        @PostConstruct
        public void init() throws InterruptedException {

                List<Document> documents =
                        documentLoaderService.loadDocuments();

                List<DocumentChunk> rawChunks =
                        chunkingService.chunk(documents);

                for(DocumentChunk chunk : rawChunks){

                        List<Double> embedding =
                                generateEmbedding(
                                        chunk.text()
                                );
                        Thread.sleep(1000);

                        chunks.add(
                                new DocumentChunk(
                                        chunk.text(),
                                        embedding,
                                        chunk.source()
                                )
                        );
                }
        }

        public RetrievalService(WebClient.Builder builder, SimilarityService similarityService, DocumentLoaderService documentLoaderService, ChunkingService chunkingService) {
                this.webClient = builder.build();
                this.similarityService=similarityService;
                this.chunkingService=chunkingService;
                this.documentLoaderService=documentLoaderService;
        }

        public List<Double> generateEmbedding(String text){
                // List<Double> a = List.of(1.0, 2.0, 3.0);
                // List<Double> b = List.of(2.0, 4.0, 6.0);

                // System.out.println(similarityService.cosineSimilarity(a, b));

                // List<Double> c = List.of(1.0, 0.0);
                // List<Double> d = List.of(0.0, 1.0);
                // System.out.println(similarityService.cosineSimilarity(c, d));
                // return new ArrayList<>();
                String requestBody = """
                                {
                                        "model": "models/gemini-embedding-1",
                                        "content": {
                                        "parts": [
                                        {
                                                "text": "%s"
                                        }
                                        ]
                                        }
                                        }
                        """.formatted(text);

                String response = webClient.post()
                                .uri(
                                                "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent?key="
                                                        + apiKey
                                        )
                                .bodyValue(requestBody!=null?requestBody:"")
                                .retrieve()
                                .onStatus(
                                                status -> status.isError(),
                                                res -> res.bodyToMono(String.class)
                                                        .map(error -> new RuntimeException(error))
                                        )
                                .bodyToMono(String.class)
                                .block();
                
                try{    
                        
                        JsonNode root = objectMapper.readTree(response);
                        // System.out.print(root.path("embedding").path("values").getClass());
                        ArrayNode node= (ArrayNode)root
                                .path("embedding")
                                .path("values");

                        return objectMapper.convertValue(
                                                node, 
                                                new TypeReference<ArrayList<Double>>() {}
                                                );
                                

                }catch(Exception e){
                        return new ArrayList<>();
                }
                        
        }

        public List<DocumentChunk> retrieve( String question, int topK ){
                // System.out.println(chunks.get(0).embedding().get(0));
                List<Double> questionVector = generateEmbedding(question);
                List<ScoreChunk> scoreChunks= new ArrayList<>();

                for(DocumentChunk documentChunk: chunks){
                        double score = similarityService.cosineSimilarity(questionVector, documentChunk.embedding());
                        scoreChunks.add( new ScoreChunk(documentChunk,score)); 
                }
                scoreChunks.sort((sc1,sc2)->{
                        if(sc1.score()>sc2.score())return -1;
                        else if(sc1.score()<sc2.score())return 1;
                        return 0;
                });

                return scoreChunks
                .stream()
                .limit(topK)
                .map((scoreChunk)->{
                        return scoreChunk.documentChunk();
                })
                .toList();
        }
}
