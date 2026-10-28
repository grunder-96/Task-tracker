package org.edu.pet.task_tracker_backend.service;

import lombok.RequiredArgsConstructor;
import org.edu.pet.task_tracker_backend.dto.UserInfoResponse;
import org.edu.pet.task_tracker_backend.entity.UserEntity;
import org.edu.pet.task_tracker_backend.exception.UserAlreadyTakenException;
import org.edu.pet.task_tracker_backend.exception.UserNotFoundException;
import org.edu.pet.task_tracker_backend.mapper.UserEntityMapper;
import org.edu.pet.task_tracker_backend.repository.UserEntityRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String EMAIL_UNIQUE_INDEX_NAME = "idx_users_email_lowercase";

    private final UserEntityRepository userEntityRepository;
    private final UserEntityMapper mapper;

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(Long userId) {

        return userEntityRepository.findById(userId)
                .map(mapper::toUserInfoResponseDto)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Transactional
    UserInfoResponse create(UserEntity userEntity) {

        try {

            UserEntity savedEntity = userEntityRepository.saveAndFlush(userEntity);
            return mapper.toUserInfoResponseDto(savedEntity);
        } catch (DataIntegrityViolationException e) {

            if (isEmailUniqueViolation(e)) {
                throw new UserAlreadyTakenException("User with same email already taken", e);
            }

            throw e;
        }
    }

    private boolean isEmailUniqueViolation(Throwable e) {

        for (Throwable t = e; t != null; t = t.getCause()) {

            if (t instanceof ConstraintViolationException cve
                    && EMAIL_UNIQUE_INDEX_NAME.equals(cve.getConstraintName())) {
                return true;
            }
        }
        return false;
    }
}