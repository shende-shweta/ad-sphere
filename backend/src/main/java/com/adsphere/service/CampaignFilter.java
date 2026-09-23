package com.adsphere.service;

import com.adsphere.domain.CampaignStatus;
import com.adsphere.domain.Objective;
import java.time.LocalDate;

/** Optional filters for the campaign list. Null fields are ignored. */
public class CampaignFilter {
  public CampaignStatus status;
  public Objective objective;
  public String name;
  /** Campaigns running on or after this date. */
  public LocalDate from;
  /** Campaigns running on or before this date. */
  public LocalDate to;
}
