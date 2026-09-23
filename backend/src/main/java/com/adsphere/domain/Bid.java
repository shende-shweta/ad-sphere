package com.adsphere.domain;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "bids")
public class Bid extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "deal_id", nullable = false)
  private Deal deal;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "campaign_id")
  private Campaign campaign;

  @Column(nullable = false, length = 64)
  private String bidder;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private BidStatus status;

  public Bid() {}

  public Bid(Deal deal, Campaign campaign, String bidder, BigDecimal amount, BidStatus status) {
    this.deal = deal;
    this.campaign = campaign;
    this.bidder = bidder;
    this.amount = amount;
    this.status = status;
  }

  public Deal getDeal() {
    return deal;
  }

  public Campaign getCampaign() {
    return campaign;
  }

  public String getBidder() {
    return bidder;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public BidStatus getStatus() {
    return status;
  }
}
