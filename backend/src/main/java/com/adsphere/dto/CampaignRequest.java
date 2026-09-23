package com.adsphere.dto;

import com.adsphere.domain.CampaignStatus;
import com.adsphere.domain.Objective;
import com.adsphere.validation.DateRange;
import com.adsphere.validation.ValidDateRange;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@ValidDateRange
public class CampaignRequest implements DateRange {

  @NotBlank(message = "Campaign name is required")
  @Size(max = 120, message = "Campaign name must be at most 120 characters")
  public String name;

  @NotNull(message = "Objective is required")
  public Objective objective;

  @NotNull(message = "Status is required")
  public CampaignStatus status;

  @NotNull(message = "Start date is required")
  public LocalDate startDate;

  @NotNull(message = "End date is required")
  public LocalDate endDate;

  @NotNull(message = "Budget is required")
  @DecimalMin(value = "100.00", message = "Budget must be at least 100")
  @DecimalMax(value = "100000000.00", message = "Budget must not exceed 100,000,000")
  @Digits(integer = 12, fraction = 2, message = "Budget may have at most 2 decimal places")
  public BigDecimal budget;

  @Size(max = 1000, message = "Description must be at most 1000 characters")
  public String description;

  /** Existing placements to assign to the campaign. */
  public List<Long> placementIds = new ArrayList<>();

  /** Optional placement created and assigned in the same transaction. */
  @Valid public PlacementRequest newPlacement;

  @Override
  public LocalDate startDate() {
    return startDate;
  }

  @Override
  public LocalDate endDate() {
    return endDate;
  }
}
