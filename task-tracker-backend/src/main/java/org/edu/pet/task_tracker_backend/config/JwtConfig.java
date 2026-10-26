package org.edu.pet.task_tracker_backend.config;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.edu.pet.task_tracker_backend.security.JwtTokenProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@EnableConfigurationProperties(JwtProperties.class)
@Configuration
@RequiredArgsConstructor
public class JwtConfig {

    private final JwtProperties jwtProperties;

    @Bean
    public JwtTokenProvider jwtTokenProvider() {

        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret()));
        return new JwtTokenProvider(key, jwtProperties.accessTtl());
    }
}