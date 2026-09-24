package com.adsphere.dto;

import com.adsphere.domain.Audience;
import com.adsphere.domain.AudienceStatus;
import com.adsphere.domain.AudienceType;
import java.time.Instant;

public class AudienceResponse {
  public final Long id;
  public final String name;
  public final AudienceType type;
  public final String description;
  public final AudienceStatus status;
  public final long estimatedSize;
  public final long version;
  public final long placementCount;
  public final Instant createdAt;
  public final Instant updatedAt;

  private AudienceResponse(Audience a, long placementCount) {
    this.id = a.getId();
    this.name = a.getName();
    this.type = a.getType();
    this.description = a.getDescription();
    this.status = a.getStatus();
    this.estimatedSize = a.getEstimatedSize();
    this.version = a.getVersion();
    this.placementCount = placementCount;
    this.createdAt = a.getCreatedAt();
    this.updatedAt = a.getUpdatedAt();
  }

  public static AudienceResponse from(Audience audience) {
    return new AudienceResponse(audience, 0L);
  }

  public static AudienceResponse from(Audience audience, long placementCount) {
    return new AudienceResponse(audience, placementCount);
  }
}
