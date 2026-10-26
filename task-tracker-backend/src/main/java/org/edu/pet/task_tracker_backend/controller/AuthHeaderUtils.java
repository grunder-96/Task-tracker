package org.edu.pet.task_tracker_backend.controller;

import lombok.experimental.UtilityClass;
import org.edu.pet.task_tracker_backend.dto.AuthTokensResponse;
import org.springframework.http.HttpHeaders;

@UtilityClass
public class AuthHeaderUtils {

    HttpHeaders buildAuthHeaders(AuthTokensResponse authTokensResponse) {

        HttpHeaders headers = new HttpHeaders();

        headers.setBearerAuth(authTokensResponse.accessToken());

        return headers;
    }
}