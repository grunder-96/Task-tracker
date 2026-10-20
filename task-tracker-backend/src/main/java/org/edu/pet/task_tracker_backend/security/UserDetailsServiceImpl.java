package org.edu.pet.task_tracker_backend.security;

import lombok.RequiredArgsConstructor;
import org.edu.pet.task_tracker_backend.mapper.UserEntityMapper;
import org.edu.pet.task_tracker_backend.repository.UserEntityRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserEntityRepository userEntityRepository;
    private final UserEntityMapper userEntityMapper;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {

        return userEntityRepository.findByEmailIgnoreCase(email)
                .map(userEntityMapper::toCustomUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("User with email %s not found".formatted(email)));
    }
}