package org.edu.pet.task_tracker_scheduler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu.pet.task_tracker_scheduler.dto.SummarizationRequest;
import org.edu.pet.task_tracker_scheduler.dto.SummarizationResponse;
import org.edu.pet.task_tracker_scheduler.model.User;
import org.edu.pet.task_tracker_scheduler.model.UserTask;
import org.edu.pet.task_tracker_scheduler.repository.ReportDataRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportOrchestratorService {

    private final ReportDataRepository reportDataRepository;
    private final KafkaSummarizationReplyService summarizationService;
    private final KafkaEmailSendingService emailSendingService;

    public void sendReports(Instant start, Instant end) {

        Map<User, List<UserTask>> tasksMap = reportDataRepository.findTasksForReportMap(start, end);

        int sent = 0;
        int skipped = 0;

        for (Map.Entry<User, List<UserTask>> entry : tasksMap.entrySet()) {

            User user = entry.getKey();

            try {

                SummarizationRequest summarizationRequest = prepareSummarizationRequest(user.id(), end, entry.getValue());
                SummarizationResponse summarizationResponse = summarizationService.getSummarizationReply(summarizationRequest);

                if (!summarizationResponse.success()) {

                    log.warn("Summarization failed for user {}: {}", user.id(), summarizationResponse.errorMessage());
                    skipped++;
                    continue;
                }

                emailSendingService.sendToKafka(user, summarizationResponse.report());
                sent++;

            } catch (RuntimeException e) {

                log.warn("Report skipped for user {}: {}", user.id(), e.getMessage());
                skipped++;
            }

        }

        log.info("Reports sending finished: sent={}, skipped={}", sent, skipped);
    }

    private UUID buildSummarizationRequestId(Long userId, Instant end) {
        return UUID.nameUUIDFromBytes((userId + ":" + end).getBytes(StandardCharsets.UTF_8));
    }

    private SummarizationRequest prepareSummarizationRequest(Long userId, Instant end, List<UserTask> tasks) {

        UUID requestId = buildSummarizationRequestId(userId, end);
        return new SummarizationRequest(requestId, tasks);
    }
}