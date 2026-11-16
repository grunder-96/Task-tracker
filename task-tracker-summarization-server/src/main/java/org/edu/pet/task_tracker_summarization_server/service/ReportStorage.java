package org.edu.pet.task_tracker_summarization_server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu.pet.task_tracker_summarization_server.entity.ReportEntity;
import org.edu.pet.task_tracker_summarization_server.repository.ReportRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportStorage {

    private final ReportRepository reportRepository;

    public Optional<String> find(UUID id) {

        try {

            Optional<String> maybeReport = reportRepository.findById(id)
                    .map(ReportEntity::getReport);

            if (maybeReport.isPresent()) {
                log.info("Report for request {} found in storage.", id);
            } else {
                log.info("Report for request {} not found in storage.", id);
            }

            return maybeReport;
        } catch (RuntimeException e) {

            log.error("Report lookup failed for request {}", id, e);
            return Optional.empty();
        }
    }

    public void save(UUID id, String report) {

        try {
            reportRepository.save(new ReportEntity(id, report));
        } catch (RuntimeException e) {
            log.error("Failed to save report for request {}", id, e);
        }
    }
}