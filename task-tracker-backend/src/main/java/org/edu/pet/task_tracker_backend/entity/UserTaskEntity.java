package org.edu.pet.task_tracker_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;

import java.time.Instant;

@Entity
@DynamicUpdate
@Table(name = "user_tasks")
@NoArgsConstructor
@Getter
@Setter
public class UserTaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description = "";

    @Column(nullable = false)
    @Setter(AccessLevel.NONE)
    private boolean done;

    @Setter(AccessLevel.NONE)
    private Instant doneAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public void changeDone(boolean newDone, Instant instant) {

        if (newDone == done) {
            return;
        }

        done = newDone;
        doneAt = newDone ? instant : null;
    }
}