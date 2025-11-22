package com.alok.ai.creditmemo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

import java.util.Objects;

/**
 * Configuration for Spring AI components
 */
@Configuration
public class SpringAiConfig {
    
    /**
     * Configure ChatClient bean for injection
     */
    @Bean
    public ChatClient chatClient(@NonNull ChatModel chatModel) {
        Objects.requireNonNull(chatModel, "ChatModel must not be null");
        return ChatClient.builder(chatModel)
            .defaultSystem("""
                You are a professional financial document specialist with expertise in 
                generating credit memos and other financial documents. You always:
                - Use precise financial language
                - Ensure accurate calculations
                - Follow standard business document formats
                - Include all required legal and regulatory information
                - Maintain professional tone
                """)
            .build();
    }
}
