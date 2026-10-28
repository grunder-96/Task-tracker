package org.edu.pet.task_tracker_backend.repository;

import org.edu.pet.task_tracker_backend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findById(Long id);

    @Query("select u from UserEntity u where lower(u.email) = lower(:email)")
    Optional<UserEntity> findByEmailIgnoreCase(@Param("email") String email);
}