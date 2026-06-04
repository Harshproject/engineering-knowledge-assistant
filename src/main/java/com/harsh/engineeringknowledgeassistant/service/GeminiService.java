package com.harsh.engineeringknowledgeassistant.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.harsh.engineeringknowledgeassistant.dto.gemini.Content;
import com.harsh.engineeringknowledgeassistant.dto.gemini.GeminiRequest;
import com.harsh.engineeringknowledgeassistant.dto.gemini.Part;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GeminiService {

        private final WebClient webClient;
        private final ObjectMapper objectMapper = new ObjectMapper();
        // private final RetrievalService retrievalService;

        @Value("${gemini.api.key}")
        private String apiKey;

        public GeminiService(WebClient.Builder builder) {
                this.webClient = builder.build();
        }

        // public String askGemini(String question) {
        // return "Gemini received: " + question;
        // }

        // public String askGemini(String question) {

        // GeminiRequest request = new GeminiRequest(
        // List.of( new Content(
        // List.of( new Part(question))
        // )
        // )
        // );

        // return request.toString();
        // }

        public String askGemini(String question) {

                GeminiRequest request =new GeminiRequest(List.of(new Content(List.of(new Part(question)))));

                String response = webClient.post()
                        .uri(
                                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                                        + apiKey
                        )
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                try{    
                        JsonNode root = objectMapper.readTree(response);
                        return root
                                .path("candidates")
                                .get(0)
                                .path("content")
                                .path("parts")
                                .get(0)
                                .path("text")
                                .asText();

                }catch(Exception e){
                        return "Error parsing Gemini response";
                }
        }

        // public String generateEmbedding(String text) {

        //         String requestBody = """
        //                 {
        //                 "model": "models/text-embedding-004",
        //                 "content": {
        //                 "parts": [
        //                 {
        //                         "text": "%s"
        //                 }
        //                 ]
        //                 }
        //                 }
        //                 """.formatted(text);

        //         return webClient.post()
        //                 .uri(
        //                         "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent?key="
        //                                 + apiKey
        //                 )
        //                 .header("Content-Type", "application/json")
        //                 .bodyValue(requestBody==null?"":requestBody)
        //                 .retrieve()
        //                 .onStatus(
        //                         status -> status.isError(),
        //                         response -> response.bodyToMono(String.class)
        //                                 .map(error -> new RuntimeException(error))
        //                 )
        //                 .bodyToMono(String.class)
        //                 .block();
        // }

        public String listModels() {

                return webClient.get()
                        .uri(
                                "https://generativelanguage.googleapis.com/v1beta/models?key="
                                        + apiKey
                        )
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
        }


}