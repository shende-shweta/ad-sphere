package com.adsphere.dto;

import com.adsphere.domain.ActivityEvent;
import java.time.Instant;

public class ActivityEventResponse {
  public final Long id;
  public final String actor;
  public final String action;
  public final String entityType;
  public final Long entityId;
  public final String entityName;
  public final String summary;
  public final Instant timestamp;

  public ActivityEventResponse(Long id, String actor, String action, String entityType,
      Long entityId, String entityName, String summary, Instant timestamp) {
    this.id = id;
    this.actor = actor;
    this.action = action;
    this.entityType = entityType;
    this.entityId = entityId;
    this.entityName = entityName;
    this.summary = summary;
    this.timestamp = timestamp;
  }

  public static ActivityEventResponse from(ActivityEvent e) {
    return new ActivityEventResponse(
        e.getId(), e.getActor(), e.getAction().name(), e.getEntityType().name(),
        e.getEntityId(), e.getEntityName(), e.getSummary(), e.getTimestamp());
  }
}
