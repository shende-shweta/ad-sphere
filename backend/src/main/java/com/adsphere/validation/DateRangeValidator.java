package com.adsphere.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, DateRange> {

  @Override
  public boolean isValid(DateRange value, ConstraintValidatorContext context) {
    if (value == null || value.startDate() == null || value.endDate() == null) {
      return true; // @NotNull on the fields reports missing values.
    }
    if (!value.endDate().isBefore(value.startDate())) {
      return true;
    }
    context.disableDefaultConstraintViolation();
    context
        .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
        .addPropertyNode("endDate")
        .addConstraintViolation();
    return false;
  }
}
