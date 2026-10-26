package org.edu.pet.task_tracker_backend.mapper;

import org.edu.pet.task_tracker_backend.dto.UserInfoResponse;
import org.edu.pet.task_tracker_backend.entity.UserEntity;
import org.edu.pet.task_tracker_backend.security.CustomUserDetails;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserEntityMapper {

    UserInfoResponse toUserInfoResponseDto(UserEntity userEntity);

    CustomUserDetails toCustomUserDetails(UserEntity userEntity);
}