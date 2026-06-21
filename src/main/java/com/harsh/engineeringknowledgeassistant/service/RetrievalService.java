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
        private final ObjectMapper objectMapper;
        private final SimilarityService similarityService;
        private final List<DocumentChunk> chunks = new ArrayList<>();
        private final EmbeddingCacheService embeddingCacheService;
        private final ChunkingService chunkingService;
        // private static final double THRESHOLD = 0.75;
        // private final List<DocumentSource> sources

        @Value("${gemini.api.key}")
        private String apiKey;

        private final List<DocumentSource> sources;

        @PostConstruct
        public void init() throws InterruptedException {

        //        List<Document> documents = new ArrayList<>();
                if(embeddingCacheService.exists()){
                        chunks.clear();
                        System.out.println("Loading embeddings from cache");
                        chunks.addAll(embeddingCacheService.load());
                        return;
                }

                System.out.println("Generating embeddings...");
                List<Document> documents =
                sources.stream()
                .flatMap(
                        source->
                                source.loadDocuments()
                                .stream()
                )
                .toList();

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
                embeddingCacheService.save(rawChunks);
        }

        public RetrievalService(WebClient.Builder builder, SimilarityService similarityService, ChunkingService chunkingService, ObjectMapper objectMapper, List<DocumentSource> sources, EmbeddingCacheService embeddingCacheService ) {
                this.webClient = builder.build();
                this.similarityService=similarityService;
                this.chunkingService=chunkingService;
                this.objectMapper=objectMapper;
                this.sources=sources;
                this.embeddingCacheService=embeddingCacheService;
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
                System.out.println("Question : "+question);

                scoreChunks.forEach(sc->{
                        System.out.println(sc.score()+" "+sc.documentChunk().source());
                });
                List<ScoreChunk> relevant = scoreChunks.stream()
                        .filter(sc -> sc.score() > 0.7)
                        .limit(topK)
                        .toList();

                return relevant.stream()
                        .map(ScoreChunk::documentChunk)
                        .toList();
                }
}
