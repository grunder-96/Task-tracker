package org.edu.pet.task_tracker_summarization_server.repository;

import org.edu.pet.task_tracker_summarization_server.entity.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReportRepository extends JpaRepository<ReportEntity, UUID> {

}