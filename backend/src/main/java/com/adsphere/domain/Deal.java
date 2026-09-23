package com.adsphere.domain;

import java.math.BigDecimal;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/** A unit of inventory offered by a publisher that advertisers can bid on. */
@Entity
@Table(name = "deals")
public class Deal extends BaseEntity {

  @Column(nullable = false, unique = true, length = 120)
  private String name;

  @Column(nullable = false, length = 120)
  private String publisher;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 24)
  private DealType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 24)
  private AdFormat adFormat;

  /** Floor CPM: the minimum bid that wins the deal. */
  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal bidPrice;

  @Column(nullable = false)
  private long availableImpressions;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private DealStatus status;

  @Column(precision = 10, scale = 2)
  private BigDecimal winningBid;

  @Column(length = 64)
  private String winningBidder;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "campaign_id")
  private Campaign campaign;

  private Instant soldAt;

  public Deal() {}

  public Deal(
      String name,
      String publisher,
      DealType type,
      AdFormat adFormat,
      BigDecimal bidPrice,
      long availableImpressions,
      DealStatus status) {
    this.name = name;
    this.publisher = publisher;
    this.type = type;
    this.adFormat = adFormat;
    this.bidPrice = bidPrice;
    this.availableImpressions = availableImpressions;
    this.status = status;
  }

  public String getName() {
    return name;
  }

  public String getPublisher() {
    return publisher;
  }

  public DealType getType() {
    return type;
  }

  public AdFormat getAdFormat() {
    return adFormat;
  }

  public BigDecimal getBidPrice() {
    return bidPrice;
  }

  public long getAvailableImpressions() {
    return availableImpressions;
  }

  public DealStatus getStatus() {
    return status;
  }

  public void setStatus(DealStatus status) {
    this.status = status;
  }

  public BigDecimal getWinningBid() {
    return winningBid;
  }

  public String getWinningBidder() {
    return winningBidder;
  }

  public Campaign getCampaign() {
    return campaign;
  }

  public Instant getSoldAt() {
    return soldAt;
  }

  /** Marks the deal as sold to the given bidder; ads for {@code campaign} will serve on it. */
  public void sell(BigDecimal amount, String bidder, Campaign campaign) {
    this.status = DealStatus.SOLD;
    this.winningBid = amount;
    this.winningBidder = bidder;
    this.campaign = campaign;
    this.soldAt = Instant.now();
  }
}
