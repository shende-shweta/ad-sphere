package com.adsphere.service;

import java.util.Collections;
import java.util.Map;

/** A request that is well-formed but violates a business rule (HTTP 422 / 409). */
public class BusinessRuleException extends RuntimeException {
  private final Map<String, String> fieldErrors;
  private final boolean conflict;

  public BusinessRuleException(String message) {
    this(message, Collections.emptyMap(), false);
  }

  public static BusinessRuleException field(String field, String message) {
    return new BusinessRuleException(message, Collections.singletonMap(field, message), false);
  }

  public static BusinessRuleException conflict(String message) {
    return new BusinessRuleException(message, Collections.emptyMap(), true);
  }

  private BusinessRuleException(String message, Map<String, String> fieldErrors, boolean conflict) {
    super(message);
    this.fieldErrors = fieldErrors;
    this.conflict = conflict;
  }

  public Map<String, String> getFieldErrors() {
    return fieldErrors;
  }

  public boolean isConflict() {
    return conflict;
  }
}
