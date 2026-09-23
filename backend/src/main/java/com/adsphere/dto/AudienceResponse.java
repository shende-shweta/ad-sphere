package com.adsphere.dto;

import com.adsphere.domain.Audience;
import com.adsphere.domain.AudienceStatus;
import com.adsphere.domain.AudienceType;

public class AudienceResponse {
  public final Long id;
  public final String name;
  public final AudienceType type;
  public final String description;
  public final AudienceStatus status;
  public final long estimatedSize;

  private AudienceResponse(Audience a) {
    this.id = a.getId();
    this.name = a.getName();
    this.type = a.getType();
    this.description = a.getDescription();
    this.status = a.getStatus();
    this.estimatedSize = a.getEstimatedSize();
  }

  public static AudienceResponse from(Audience audience) {
    return new AudienceResponse(audience);
  }
}
