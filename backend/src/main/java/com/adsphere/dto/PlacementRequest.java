package com.adsphere.dto;

import com.adsphere.domain.AdFormat;
import com.adsphere.domain.AdPosition;
import com.adsphere.domain.Country;
import com.adsphere.domain.DealType;
import com.adsphere.domain.DeviceTargeting;
import com.adsphere.domain.TrafficType;
import com.adsphere.domain.VideoTargeting;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class PlacementRequest {

  @NotBlank(message = "Placement name is required")
  @Size(max = 120, message = "Placement name must be at most 120 characters")
  public String name;

  @NotNull(message = "Country is required")
  public Country country;

  @NotNull(message = "Audience group is required")
  public Long audienceId;

  public VideoTargeting videoTargeting;

  @NotNull(message = "Traffic type is required")
  public TrafficType traffic;

  @NotNull(message = "Position of ad is required")
  public AdPosition adPosition;

  public DealType dealType;

  @Pattern(
      regexp = "^$|^[1-9]\\d{0,3}/(hour|day|week|month)$",
      message = "Frequency cap must look like 3/day (hour, day, week or month)")
  public String frequencyCap;

  public DeviceTargeting deviceTargeting;

  public AdFormat adFormat;

  @Size(max = 1000, message = "Notes must be at most 1000 characters")
  public String notes;
}
