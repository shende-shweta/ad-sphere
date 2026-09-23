package com.adsphere.validation;

import java.time.LocalDate;

public interface DateRange {
  LocalDate startDate();

  LocalDate endDate();
}
