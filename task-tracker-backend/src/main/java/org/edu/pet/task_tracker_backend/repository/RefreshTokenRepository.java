package org.edu.pet.task_tracker_backend.repository;

import org.edu.pet.task_tracker_backend.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByHash(String hash);
}