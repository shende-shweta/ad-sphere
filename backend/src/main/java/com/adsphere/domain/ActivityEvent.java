package com.adsphere.domain;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(
    name = "activity_events",
    indexes = {
      @Index(name = "idx_activity_timestamp", columnList = "timestamp DESC"),
      @Index(name = "idx_activity_composite", columnList = "entity_type, action, timestamp"),
      @Index(name = "idx_activity_actor", columnList = "actor, timestamp")
    })
public class ActivityEvent extends BaseEntity {

  @Column(nullable = false)
  private String actor;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ActivityAction action;

  @Enumerated(EnumType.STRING)
  @Column(name = "entity_type", nullable = false, length = 20)
  private EntityType entityType;

  @Column(name = "entity_id", nullable = false)
  private Long entityId;

  @Column(name = "entity_name", nullable = false, length = 500)
  private String entityName;

  @Column(nullable = false, length = 1000)
  private String summary;

  @Column(nullable = false)
  private Instant timestamp;

  protected ActivityEvent() {}

  public ActivityEvent(
      String actor,
      ActivityAction action,
      EntityType entityType,
      Long entityId,
      String entityName,
      String summary) {
    this.actor = actor;
    this.action = action;
    this.entityType = entityType;
    this.entityId = entityId;
    this.entityName = entityName;
    this.summary = summary;
    this.timestamp = Instant.now();
  }

  public String getActor() {
    return actor;
  }

  public ActivityAction getAction() {
    return action;
  }

  public EntityType getEntityType() {
    return entityType;
  }

  public Long getEntityId() {
    return entityId;
  }

  public String getEntityName() {
    return entityName;
  }

  public String getSummary() {
    return summary;
  }

  public Instant getTimestamp() {
    return timestamp;
  }
}
