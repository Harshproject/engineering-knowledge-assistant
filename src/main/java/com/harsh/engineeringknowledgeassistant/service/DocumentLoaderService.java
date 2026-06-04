package com.harsh.engineeringknowledgeassistant.service;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.harsh.engineeringknowledgeassistant.dto.gemini.Document;

@Service
public class DocumentLoaderService {

    public List<Document> loadDocuments() {

        List<Document> documents = new ArrayList<>();

        try {

            ClassPathResource resource =
                    new ClassPathResource("docs");

            File folder = resource.getFile();

            File[] files = folder.listFiles();

            if (files == null) {
                return documents;
            }

            for (File file : files) {

                if (file.getName().endsWith(".md")) {

                    String content =
                            Files.readString(file.toPath());

                    documents.add(new Document(file.getName(),content));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return documents;
    }
}