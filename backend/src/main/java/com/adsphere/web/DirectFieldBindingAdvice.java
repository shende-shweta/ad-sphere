package com.adsphere.web;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * Request DTOs expose public fields rather than getters; tell the binder to read them directly so
 * bean-validation errors can be mapped back to field names.
 */
@ControllerAdvice
public class DirectFieldBindingAdvice {

  @InitBinder
  void initDirectFieldAccess(WebDataBinder binder) {
    binder.initDirectFieldAccess();
  }
}
