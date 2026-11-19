package org.edu.pet.task_tracker_scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class TaskTrackerSchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskTrackerSchedulerApplication.class, args);
	}
}