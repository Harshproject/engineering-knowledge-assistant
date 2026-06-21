package com.harsh.engineeringknowledgeassistant.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harsh.engineeringknowledgeassistant.dto.gemini.DocumentChunk;

@Service
public class EmbeddingCacheService {

    // private final ClassPathResource resource = new ClassPathResource("cache/embedding.json");
    private final ObjectMapper mapper;
    private static final Path CACHE_PATH =
        Path.of("cache", "embeddings.json");

    public EmbeddingCacheService(
            ObjectMapper mapper
    ){

        this.mapper=mapper;

    }
    
    public boolean exists(){
        return Files.exists(CACHE_PATH);
    }



    public List<DocumentChunk> load(){
        try{
            return mapper.readValue(
                    CACHE_PATH.toFile(),
                     new TypeReference<>(){}
            );
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }



    public void save( List<DocumentChunk> chunks ){

            try{
                System.out.println("Saving embeddings to cache");
                Files.createDirectories(
                        CACHE_PATH.getParent()
                );

                System.out.println("Writing embeddings to cache" + " Size: " + chunks.size());
                mapper.writeValue(

                        CACHE_PATH.toFile(),

                        chunks

                );

            }
            catch(Exception e){

                throw new RuntimeException(e);

            }

    }

    public void refresh(){


        try{

                Files.deleteIfExists(

                        CACHE_PATH

                );

            }

            catch(Exception e){

                    throw new RuntimeException(e);

            }

    }

}
