package org.edu.pet.task_tracker_summarization_server.service;

public interface LlmTokenProvider {

    String getToken();

    void invalidate();
}