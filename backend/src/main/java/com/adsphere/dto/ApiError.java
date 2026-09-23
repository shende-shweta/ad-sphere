package com.adsphere.dto;

import java.time.Instant;
import java.util.Map;

public class ApiError {
  public final Instant timestamp = Instant.now();
  public final int status;
  public final String error;
  public final String message;
  public final String path;
  public final Map<String, String> fieldErrors;

  public ApiError(
      int status, String error, String message, String path, Map<String, String> fieldErrors) {
    this.status = status;
    this.error = error;
    this.message = message;
    this.path = path;
    this.fieldErrors = fieldErrors;
  }
}
