package com.adsphere.web;

import com.adsphere.dto.ApiError;
import com.adsphere.service.BusinessRuleException;
import com.adsphere.service.NotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiError> validation(MethodArgumentNotValidException e, HttpServletRequest req) {
    Map<String, String> fields = new LinkedHashMap<>();
    for (FieldError error : e.getBindingResult().getFieldErrors()) {
      fields.putIfAbsent(error.getField(), error.getDefaultMessage());
    }
    e.getBindingResult()
        .getGlobalErrors()
        .forEach(g -> fields.putIfAbsent(g.getObjectName(), g.getDefaultMessage()));
    return build(HttpStatus.BAD_REQUEST, "Please correct the highlighted fields", req, fields);
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class,
    PropertyReferenceException.class
  })
  ResponseEntity<ApiError> badRequest(Exception e, HttpServletRequest req) {
    return build(HttpStatus.BAD_REQUEST, "The request is malformed or has invalid values", req, null);
  }

  @ExceptionHandler(NotFoundException.class)
  ResponseEntity<ApiError> notFound(NotFoundException e, HttpServletRequest req) {
    return build(HttpStatus.NOT_FOUND, e.getMessage(), req, null);
  }

  @ExceptionHandler(BusinessRuleException.class)
  ResponseEntity<ApiError> business(BusinessRuleException e, HttpServletRequest req) {
    HttpStatus status = e.isConflict() ? HttpStatus.CONFLICT : HttpStatus.UNPROCESSABLE_ENTITY;
    return build(
        status, e.getMessage(), req, e.getFieldErrors().isEmpty() ? null : e.getFieldErrors());
  }

  @ExceptionHandler(OptimisticLockingFailureException.class)
  ResponseEntity<ApiError> conflict(Exception e, HttpServletRequest req) {
    return build(
        HttpStatus.CONFLICT, "This record was changed by someone else. Reload and retry.", req, null);
  }

  @ExceptionHandler({BadCredentialsException.class, DisabledException.class})
  ResponseEntity<ApiError> badCredentials(Exception e, HttpServletRequest req) {
    return build(HttpStatus.UNAUTHORIZED, "Invalid username or password", req, null);
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<ApiError> forbidden(AccessDeniedException e, HttpServletRequest req) {
    return build(HttpStatus.FORBIDDEN, "You do not have permission to do this", req, null);
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiError> unexpected(Exception e, HttpServletRequest req) {
    log.error("Unhandled error on {} {}", req.getMethod(), req.getRequestURI(), e);
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong", req, null);
  }

  private static ResponseEntity<ApiError> build(
      HttpStatus status, String message, HttpServletRequest req, Map<String, String> fields) {
    return ResponseEntity.status(status)
        .body(new ApiError(status.value(), status.getReasonPhrase(), message, req.getRequestURI(), fields));
  }
}
