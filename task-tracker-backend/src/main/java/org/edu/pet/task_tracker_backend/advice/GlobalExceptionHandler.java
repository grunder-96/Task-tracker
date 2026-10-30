package org.edu.pet.task_tracker_backend.advice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu.pet.task_tracker_backend.controller.RefreshCookieFactory;
import org.edu.pet.task_tracker_backend.dto.ErrorResponse;
import org.edu.pet.task_tracker_backend.exception.InvalidRefreshTokenException;
import org.edu.pet.task_tracker_backend.exception.UserAlreadyTakenException;
import org.edu.pet.task_tracker_backend.exception.UserNotFoundException;
import org.edu.pet.task_tracker_backend.exception.UserTaskNotFoundException;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final RefreshCookieFactory refreshCookieFactory;

    @ExceptionHandler(UserAlreadyTakenException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleUserAlreadyTakenException(UserAlreadyTakenException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler({UserNotFoundException.class, UserTaskNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFoundException(RuntimeException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleBadCredentialsException(BadCredentialsException e) {
        return new ErrorResponse("Invalid email or password");
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshTokenException(InvalidRefreshTokenException e) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.SET_COOKIE, refreshCookieFactory.clear().toString())
                .body(new ErrorResponse(e.getMessage()));
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        LinkedHashMap::new,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.joining(", "))
                ))
                .entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("; "));

        ErrorResponse errorResponse = new ErrorResponse(errorMessage);

        return ResponseEntity
                .status(status)
                .headers(headers)
                .body(errorResponse);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {

        String message;

        if (statusCode.is5xxServerError()) {

            log.error("Unhandled server error", ex);
            message = getReasonPhrase(statusCode);
        } else {

            message = ex instanceof org.springframework.web.ErrorResponse errorResponse && errorResponse.getBody().getDetail() != null ?
                errorResponse.getBody().getDetail() : getReasonPhrase(statusCode);
        }

        return ResponseEntity
                .status(statusCode)
                .headers(headers)
                .body(new ErrorResponse(message));
    }

    private String getReasonPhrase(HttpStatusCode status) {

        HttpStatus httpStatus = HttpStatus.resolve(status.value());

        if (httpStatus != null) {
            return httpStatus.getReasonPhrase();
        }

        return status.is5xxServerError() ? "Internal server error" : "error";
    }
}