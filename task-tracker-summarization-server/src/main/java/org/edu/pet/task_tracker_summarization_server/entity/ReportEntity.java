package org.edu.pet.task_tracker_summarization_server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "summarization_reports")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ReportEntity {

    @Id
    private UUID requestId;

    @Column(nullable = false)
    private String report;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    public ReportEntity(UUID requestId, String report) {

        this.requestId = requestId;
        this.report = report;
    }
}