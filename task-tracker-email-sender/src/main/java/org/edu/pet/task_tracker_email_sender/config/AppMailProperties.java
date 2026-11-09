package org.edu.pet.task_tracker_email_sender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public record AppMailProperties(String from) {

}