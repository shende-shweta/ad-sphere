package com.adsphere.dto;

import com.adsphere.domain.ActivityAction;
import com.adsphere.domain.ActivityEvent;
import com.adsphere.domain.EntityType;
import java.time.Instant;

public class ActivityEventResponse {
  public final Long id;
  public final String actor;
  public final ActivityAction action;
  public final EntityType entityType;
  public final Long entityId;
  public final String entityName;
  public final String summary;
  public final Instant timestamp;

  private ActivityEventResponse(ActivityEvent e) {
    this.id = e.getId();
    this.actor = e.getActor();
    this.action = e.getAction();
    this.entityType = e.getEntityType();
    this.entityId = e.getEntityId();
    this.entityName = e.getEntityName();
    this.summary = e.getSummary();
    this.timestamp = e.getTimestamp();
  }

  public static ActivityEventResponse from(ActivityEvent event) {
    return new ActivityEventResponse(event);
  }
}
