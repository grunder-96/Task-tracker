package org.edu.pet.task_tracker_backend.mapper;

import org.edu.pet.task_tracker_backend.dto.UserInfoResponseDto;
import org.edu.pet.task_tracker_backend.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserEntityMapper {

    UserInfoResponseDto toUserInfoResponseDto(UserEntity userEntity);
}