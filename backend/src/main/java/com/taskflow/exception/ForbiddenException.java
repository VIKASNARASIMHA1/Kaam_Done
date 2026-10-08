package com.taskflow.exception;

/**
 * Thrown when an authenticated user is recognized but does not have
 * sufficient permission for the action (e.g. a VIEWER trying to edit,
 * or a non-owner trying to manage members). Deliberately NOT
 * org.springframework.security.access.AccessDeniedException, because that
 * type gets intercepted by Spring Security's ExceptionTranslationFilter
 * before it reaches our @RestControllerAdvice, which would strip out our
 * custom message and return an empty 403 body instead.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
