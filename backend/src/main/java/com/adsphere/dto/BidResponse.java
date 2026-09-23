package com.adsphere.dto;

import com.adsphere.domain.BidStatus;
import java.math.BigDecimal;

public class BidResponse {
  public final Long bidId;
  public final BidStatus status;
  public final BigDecimal amount;
  public final String message;
  public final DealResponse deal;

  public BidResponse(
      Long bidId, BidStatus status, BigDecimal amount, String message, DealResponse deal) {
    this.bidId = bidId;
    this.status = status;
    this.amount = amount;
    this.message = message;
    this.deal = deal;
  }
}
