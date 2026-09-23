package com.adsphere.dto;

import com.adsphere.domain.AudienceStatus;
import com.adsphere.domain.AudienceType;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class AudienceRequest {

  @NotBlank(message = "Audience name is required")
  @Size(min = 3, max = 120, message = "Audience name must be 3-120 characters")
  public String name;

  @NotNull(message = "Type is required")
  public AudienceType type;

  @Size(max = 500, message = "Description must be at most 500 characters")
  public String description;

  @NotNull(message = "Estimated reach is required")
  @Min(value = 1000, message = "Estimated reach must be between 1,000 and 10,000,000,000")
  @Max(value = 10000000000L, message = "Estimated reach must be between 1,000 and 10,000,000,000")
  public Long estimatedSize;

  public AudienceStatus status;

  public Long version;
}
