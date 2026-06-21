# Engineering Knowledge Assistant

A Spring Boot application that provides a simple engineering knowledge assistant backed by Google Gemini embeddings and generation APIs.

## Overview

This project loads markdown documentation from `src/main/resources/docs`, splits the content into chunks, generates embeddings with Gemini, and exposes HTTP endpoints for:

- asking direct questions to Gemini
- generating embeddings for text
- retrieving available Gemini models
- answering questions using retrieval-augmented generation (RAG)
- listing loaded documents and chunks

## Key Components

- `ChatController` — REST endpoints under `/chat`
- `GeminiService` — calls Gemini generation APIs
- `RetrievalService` — loads docs, creates embeddings, and retrieves similar chunks
- `RagService` — builds a prompt using retrieved chunks and asks Gemini for an answer
- `DocumentLoaderService` — loads `.md` files from `resources/docs`
- `ChunkingService` — splits document text into paragraph chunks
- `SimilarityService` — computes cosine similarity for ranking chunks

## Requirements

- Java 21
- Maven
- A valid Gemini API key configured as `GEMINI_API_KEY`

## Configuration

The application reads configuration from `src/main/resources/application.properties`.

```properties
spring.application.name=engineering-knowledge-assistant
gemini.api.key=${GEMINI_API_KEY}
```

Set the environment variable before running:

```bash
export GEMINI_API_KEY="your_gemini_api_key"
```

## Build and Run

From the repository root:

```bash
./mvnw clean package
./mvnw spring-boot:run
```

Or run the packaged jar:

```bash
./mvnw package
java -jar target/engineering-knowledge-assistant-0.0.1-SNAPSHOT.jar
```

## API Endpoints

### POST /chat/ask

Ask a direct question to Gemini.

Request body:

```json
{
  "question": "What is the inventory service?"
}
```

Response: plain text answer from Gemini.

### POST /chat/embedding

Generate an embedding for input text.

Request body:

```json
{
  "text": "Some engineering question or statement"
}
```

Response: JSON array of embedding values.

### GET /chat/models

Returns the Gemini model list from the Google API.

### POST /chat/ragservice

Ask a question using retrieval-augmented generation.

Request body:

```json
{
  "text": "How does payments work in the docs?"
}
```

Response: plain text answer constructed from retrieved document chunk context.

### GET /chat/docs

Returns loaded documents from `src/main/resources/docs` as JSON objects with `fileName` and `content`.

### GET /chat/chunks

Returns document chunks extracted from loaded markdown files.

## Notes

- `RetrievalService` creates embeddings during application startup using documents in `resources/docs`.
- The chunking logic splits markdown content by double newlines.
- The RAG workflow retrieves top matching chunks and builds a Gemini prompt with context.
- `GeminiService` sends requests to Google Gemini endpoints using `WebClient`.

## Project Structure

- `src/main/java/com/harsh/engineeringknowledgeassistant`
  - `controller/ChatController.java`
  - `dto/gemini/` request and response DTO records
  - `service/` core application logic
- `src/main/resources/docs/` Markdown files used for knowledge retrieval

## Extending the Project

To extend this project, consider:

- adding error handling for external API failures
- caching embeddings and responses
- supporting additional document formats
- adding request validation
- extracting prompt templates to configuration

## Testing

Run tests with:

```bash
./mvnw test
```
