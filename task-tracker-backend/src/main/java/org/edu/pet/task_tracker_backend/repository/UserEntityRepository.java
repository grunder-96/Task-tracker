package org.edu.pet.task_tracker_backend.repository;

import org.edu.pet.task_tracker_backend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserEntityRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findById(Long id);

    Optional<UserEntity> findByEmailIgnoreCase(String email);
}