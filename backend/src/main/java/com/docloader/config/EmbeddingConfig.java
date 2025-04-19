package com.docloader.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.document.Document;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * Configuration for EmbeddingModel when Spring AI dependencies are not available
 */
@Configuration
@Slf4j
public class EmbeddingConfig {

    /**
     * Fallback EmbeddingModel that provides a stub implementation when
     * no other EmbeddingModel bean is available.
     * Will be replaced by a real implementation from Spring AI.
     */
    @Bean
    @Primary
    public EmbeddingModel fallbackEmbeddingModel() {
        log.warn("Using fallback EmbeddingModel - no Spring AI embedding model is configured");
        
        return new EmbeddingModel() {
            @Override
            public EmbeddingResponse call(EmbeddingRequest request) {
                log.info("Stub implementation: Processing embedding request");
                // Return a dummy embedding response
                return null;
            }
            
            @Override
            public float[] embed(String text) {
                log.info("Stub implementation: Generating embedding for text: {}", text);
                // Return a dummy embedding vector with 10 dimensions
                return new float[10];
            }

            @Override
            public float[] embed(Document document) {
                log.info("Stub implementation: Generating embedding for document");
                // Return a dummy embedding vector with 10 dimensions
                return new float[10];
            }

            @Override
            public List<float[]> embed(List<String> texts) {
                log.info("Stub implementation: Generating embeddings for {} texts", texts.size());
                // Return dummy embedding vectors with 10 dimensions
                return texts.stream()
                        .map(text -> new float[10])
                        .toList();
            }
            
            @Override
            public int dimensions() {
                return 10;
            }
        };
    }
} 