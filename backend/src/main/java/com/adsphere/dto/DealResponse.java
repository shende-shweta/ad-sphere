package com.adsphere.dto;

import com.adsphere.domain.AdFormat;
import com.adsphere.domain.Deal;
import com.adsphere.domain.DealStatus;
import com.adsphere.domain.DealType;
import java.math.BigDecimal;
import java.time.Instant;

public class DealResponse {
  public final Long id;
  public final String name;
  public final String publisher;
  public final DealType type;
  public final AdFormat adFormat;
  public final BigDecimal bidPrice;
  public final long availableImpressions;
  public final DealStatus status;
  public final BigDecimal winningBid;
  public final String winningBidder;
  public final Long campaignId;
  public final String campaignName;
  public final Instant soldAt;

  private DealResponse(Deal d) {
    this.id = d.getId();
    this.name = d.getName();
    this.publisher = d.getPublisher();
    this.type = d.getType();
    this.adFormat = d.getAdFormat();
    this.bidPrice = d.getBidPrice();
    this.availableImpressions = d.getAvailableImpressions();
    this.status = d.getStatus();
    this.winningBid = d.getWinningBid();
    this.winningBidder = d.getWinningBidder();
    this.campaignId = d.getCampaign() == null ? null : d.getCampaign().getId();
    this.campaignName = d.getCampaign() == null ? null : d.getCampaign().getName();
    this.soldAt = d.getSoldAt();
  }

  public static DealResponse from(Deal deal) {
    return new DealResponse(deal);
  }
}
