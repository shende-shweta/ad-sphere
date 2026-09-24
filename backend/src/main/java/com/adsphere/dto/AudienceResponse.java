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
  public final long version;
  public final long placementCount;
  public final String createdAt;
  public final String updatedAt;

  private AudienceResponse(Audience a, long placementCount) {
    this.id = a.getId();
    this.name = a.getName();
    this.type = a.getType();
    this.description = a.getDescription();
    this.status = a.getStatus();
    this.estimatedSize = a.getEstimatedSize();
    this.version = a.getVersion() != null ? a.getVersion() : 0L;
    this.placementCount = placementCount;
    this.createdAt = a.getCreatedAt() != null ? a.getCreatedAt().toString() : null;
    this.updatedAt = a.getUpdatedAt() != null ? a.getUpdatedAt().toString() : null;
  }

  public static AudienceResponse from(Audience audience, long placementCount) {
    return new AudienceResponse(audience, placementCount);
  }

  public static AudienceResponse from(Audience audience) {
    return new AudienceResponse(audience, 0);
  }
}
