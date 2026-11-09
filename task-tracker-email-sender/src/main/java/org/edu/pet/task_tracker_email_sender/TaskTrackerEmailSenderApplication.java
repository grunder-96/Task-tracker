package org.edu.pet.task_tracker_email_sender;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TaskTrackerEmailSenderApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskTrackerEmailSenderApplication.class, args);
	}
}