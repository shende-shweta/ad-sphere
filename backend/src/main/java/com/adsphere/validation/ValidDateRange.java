package com.adsphere.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/** Ensures {@link DateRange#endDate()} is not before {@link DateRange#startDate()}. */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateRangeValidator.class)
public @interface ValidDateRange {
  String message() default "End date must be on or after the start date";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
