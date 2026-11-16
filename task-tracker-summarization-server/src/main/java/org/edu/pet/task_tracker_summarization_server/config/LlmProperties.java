package org.edu.pet.task_tracker_summarization_server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.llm")
public record LlmProperties(String authKey,
                            String authUrl,
                            String apiBaseUrl,
                            String modelName) {

}