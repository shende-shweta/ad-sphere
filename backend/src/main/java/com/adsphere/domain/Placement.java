package com.adsphere.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "placements")
public class Placement extends BaseEntity {

  @Column(nullable = false, length = 120)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 4)
  private Country country;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "audience_id", nullable = false)
  private Audience audience;

  @Enumerated(EnumType.STRING)
  @Column(length = 24)
  private VideoTargeting videoTargeting;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private TrafficType traffic;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 24)
  private AdPosition adPosition;

  @Enumerated(EnumType.STRING)
  @Column(length = 24)
  private DealType dealType;

  /** Frequency cap in the form {@code <count>/<period>}, e.g. 3/day. */
  @Column(length = 24)
  private String frequencyCap;

  @Enumerated(EnumType.STRING)
  @Column(length = 24)
  private DeviceTargeting deviceTargeting;

  @Enumerated(EnumType.STRING)
  @Column(length = 24)
  private AdFormat adFormat;

  @Column(length = 1000)
  private String notes;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Country getCountry() {
    return country;
  }

  public void setCountry(Country country) {
    this.country = country;
  }

  public Audience getAudience() {
    return audience;
  }

  public void setAudience(Audience audience) {
    this.audience = audience;
  }

  public VideoTargeting getVideoTargeting() {
    return videoTargeting;
  }

  public void setVideoTargeting(VideoTargeting videoTargeting) {
    this.videoTargeting = videoTargeting;
  }

  public TrafficType getTraffic() {
    return traffic;
  }

  public void setTraffic(TrafficType traffic) {
    this.traffic = traffic;
  }

  public AdPosition getAdPosition() {
    return adPosition;
  }

  public void setAdPosition(AdPosition adPosition) {
    this.adPosition = adPosition;
  }

  public DealType getDealType() {
    return dealType;
  }

  public void setDealType(DealType dealType) {
    this.dealType = dealType;
  }

  public String getFrequencyCap() {
    return frequencyCap;
  }

  public void setFrequencyCap(String frequencyCap) {
    this.frequencyCap = frequencyCap;
  }

  public DeviceTargeting getDeviceTargeting() {
    return deviceTargeting;
  }

  public void setDeviceTargeting(DeviceTargeting deviceTargeting) {
    this.deviceTargeting = deviceTargeting;
  }

  public AdFormat getAdFormat() {
    return adFormat;
  }

  public void setAdFormat(AdFormat adFormat) {
    this.adFormat = adFormat;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }
}
