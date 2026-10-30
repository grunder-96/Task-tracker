package org.edu.pet.task_tracker_backend.repository;

import org.edu.pet.task_tracker_backend.entity.UserTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserTaskRepository extends JpaRepository<UserTaskEntity, Long> {

    List<UserTaskEntity> findAllByUserId(Long userId);

    Optional<UserTaskEntity> findByIdAndUserId(Long taskId, Long userId);

    @Modifying
    @Query("DELETE FROM UserTaskEntity t WHERE t.id = :taskId AND t.userId = :userId")
    int deleteByIdAndUserId(@Param("taskId") Long taskId, @Param("userId") Long userId);
}