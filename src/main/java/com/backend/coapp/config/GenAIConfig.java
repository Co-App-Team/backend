package com.backend.coapp.config;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GenAIConfig {
  @Bean
  @ConditionalOnProperty(name = "gen-ai.provider", havingValue = "gemini")
  public Client geminiClient(@Value("${google.genai.api.key}") String apiKey) {
    return new Client.Builder().apiKey(apiKey).build();
  }
}
