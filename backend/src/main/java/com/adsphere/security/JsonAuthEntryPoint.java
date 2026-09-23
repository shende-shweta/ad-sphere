package com.adsphere.security;

import com.adsphere.dto.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/** Writes 401/403 responses as JSON {@link ApiError}s instead of HTML error pages. */
@Component
public class JsonAuthEntryPoint implements AuthenticationEntryPoint, AccessDeniedHandler {

  private final ObjectMapper mapper;

  public JsonAuthEntryPoint(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public void commence(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
      throws IOException {
    write(response, request, HttpStatus.UNAUTHORIZED, "Authentication is required");
  }

  @Override
  public void handle(
      HttpServletRequest request, HttpServletResponse response, AccessDeniedException e)
      throws IOException {
    write(response, request, HttpStatus.FORBIDDEN, "You do not have permission to do this");
  }

  private void write(
      HttpServletResponse response, HttpServletRequest request, HttpStatus status, String message)
      throws IOException {
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
    mapper.writeValue(
        response.getOutputStream(),
        new ApiError(
            status.value(), status.getReasonPhrase(), message, request.getRequestURI(), null));
  }
}
