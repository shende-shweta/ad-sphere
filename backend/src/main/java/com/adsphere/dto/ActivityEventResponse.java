package com.adsphere.dto;

import com.adsphere.domain.ActivityEvent;
import java.time.Instant;

public class ActivityEventResponse {
  public final Long id;
  public final String action;
  public final String entityType;
  public final Long entityId;
  public final String entityName;
  public final String actor;
  public final String summary;
  public final Instant timestamp;

  private ActivityEventResponse(ActivityEvent e) {
    this.id = e.getId();
    this.action = e.getAction().name();
    this.entityType = e.getEntityType().name();
    this.entityId = e.getEntityId();
    this.entityName = e.getEntityName();
    this.actor = e.getActor();
    this.summary = e.getSummary();
    this.timestamp = e.getTimestamp();
  }

  public static ActivityEventResponse from(ActivityEvent event) {
    return new ActivityEventResponse(event);
  }
}
