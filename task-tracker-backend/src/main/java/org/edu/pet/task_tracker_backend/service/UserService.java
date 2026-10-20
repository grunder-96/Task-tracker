package org.edu.pet.task_tracker_backend.service;

import lombok.RequiredArgsConstructor;
import org.edu.pet.task_tracker_backend.dto.UserInfoResponseDto;
import org.edu.pet.task_tracker_backend.exception.UserNotFoundException;
import org.edu.pet.task_tracker_backend.mapper.UserEntityMapper;
import org.edu.pet.task_tracker_backend.repository.UserEntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserEntityRepository userEntityRepository;

    private final UserEntityMapper mapper;

    @Transactional(readOnly = true)
    public UserInfoResponseDto getUserInfo(Long id) {

        return userEntityRepository.findById(id)
                .map(mapper::toUserInfoResponseDto)
                .orElseThrow(() -> new UserNotFoundException(
                        "User with id %s not found".formatted(id)));
    }
}