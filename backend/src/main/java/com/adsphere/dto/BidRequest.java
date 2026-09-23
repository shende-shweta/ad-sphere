package com.adsphere.dto;

import java.math.BigDecimal;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

public class BidRequest {

  @NotNull(message = "Bid amount is required")
  @DecimalMin(value = "0.01", message = "Bid amount must be greater than 0")
  @Digits(integer = 8, fraction = 2, message = "Bid amount may have at most 2 decimal places")
  public BigDecimal amount;

  /** Campaign whose ads will serve on the inventory if the bid wins. */
  @NotNull(message = "Campaign is required")
  public Long campaignId;
}
