package org.edu.pet.task_tracker_summarization_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TaskTrackerSummarizationServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskTrackerSummarizationServerApplication.class, args);
	}
}