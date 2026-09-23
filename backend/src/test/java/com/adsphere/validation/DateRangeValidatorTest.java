package com.adsphere.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.adsphere.domain.CampaignStatus;
import com.adsphere.domain.Objective;
import com.adsphere.dto.CampaignRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.Test;

public class DateRangeValidatorTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  private static CampaignRequest request(String start, String end) {
    CampaignRequest r = new CampaignRequest();
    r.name = "Test";
    r.objective = Objective.REACH;
    r.status = CampaignStatus.DRAFT;
    r.budget = new BigDecimal("1000");
    r.startDate = LocalDate.parse(start);
    r.endDate = LocalDate.parse(end);
    return r;
  }

  @Test
  public void acceptsSameDayAndLaterEnd() {
    assertTrue(validator.validate(request("2025-01-01", "2025-01-01")).isEmpty());
    assertTrue(validator.validate(request("2025-01-01", "2025-02-01")).isEmpty());
  }

  @Test
  public void reportsEndBeforeStartOnEndDate() {
    Set<ConstraintViolation<CampaignRequest>> violations =
        validator.validate(request("2025-02-01", "2025-01-01"));
    assertEquals(1, violations.size());
    assertEquals("endDate", violations.iterator().next().getPropertyPath().toString());
  }
}
