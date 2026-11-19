package org.edu.pet.task_tracker_scheduler.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu.pet.task_tracker_scheduler.service.ReportOrchestratorService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportScheduler {

    private static final Duration REPORT_PERIOD = Duration.ofDays(1);

    private final ReportOrchestratorService orchestratorService;

    @Scheduled(cron = "${app.scheduler.cron}")
    public void run() {

        Instant end = Instant.now().truncatedTo(ChronoUnit.HOURS);
        Instant start = end.minus(REPORT_PERIOD);

        log.info("Report sending for period {} - {} started", start, end);
        orchestratorService.sendReports(start, end);
    }
}