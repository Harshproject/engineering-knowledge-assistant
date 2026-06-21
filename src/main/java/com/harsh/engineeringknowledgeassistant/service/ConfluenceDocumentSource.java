package com.harsh.engineeringknowledgeassistant.service;

import java.io.InputStream;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harsh.engineeringknowledgeassistant.dto.gemini.ConfluenceResponse;
import com.harsh.engineeringknowledgeassistant.dto.gemini.Document;

@Service
public class ConfluenceDocumentSource
implements DocumentSource{

    private final ObjectMapper mapper;
    ClassPathResource resource =
        new ClassPathResource("mock/confluence.json");

    public ConfluenceDocumentSource(
            ObjectMapper mapper
    ) {
        this.mapper = mapper;
    }


    @Override
    public List<Document> loadDocuments() {

        try {
            ClassPathResource resource =
                    new ClassPathResource("mock/confluence.json");

            InputStream is = resource.getInputStream();

            ConfluenceResponse response =
                    mapper.readValue(
                            is,
                            ConfluenceResponse.class
                    );
            return response.results()
            .stream()
            .map(cf -> new Document(
                    cf.title(),
                    cf.body()
            ))
            .toList();

        }
        catch (Exception e){
            throw new RuntimeException(e);
        }

    }

}
