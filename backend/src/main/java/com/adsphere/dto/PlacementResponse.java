package com.adsphere.dto;

import com.adsphere.domain.AdFormat;
import com.adsphere.domain.AdPosition;
import com.adsphere.domain.Country;
import com.adsphere.domain.DealType;
import com.adsphere.domain.DeviceTargeting;
import com.adsphere.domain.Placement;
import com.adsphere.domain.TrafficType;
import com.adsphere.domain.VideoTargeting;
import java.time.Instant;

public class PlacementResponse {
  public final Long id;
  public final String name;
  public final Country country;
  public final Long audienceId;
  public final String audienceName;
  public final VideoTargeting videoTargeting;
  public final TrafficType traffic;
  public final AdPosition adPosition;
  public final DealType dealType;
  public final String frequencyCap;
  public final DeviceTargeting deviceTargeting;
  public final AdFormat adFormat;
  public final String notes;
  public final Instant createdAt;

  private PlacementResponse(Placement p) {
    this.id = p.getId();
    this.name = p.getName();
    this.country = p.getCountry();
    this.audienceId = p.getAudience().getId();
    this.audienceName = p.getAudience().getName();
    this.videoTargeting = p.getVideoTargeting();
    this.traffic = p.getTraffic();
    this.adPosition = p.getAdPosition();
    this.dealType = p.getDealType();
    this.frequencyCap = p.getFrequencyCap();
    this.deviceTargeting = p.getDeviceTargeting();
    this.adFormat = p.getAdFormat();
    this.notes = p.getNotes();
    this.createdAt = p.getCreatedAt();
  }

  public static PlacementResponse from(Placement placement) {
    return new PlacementResponse(placement);
  }
}
