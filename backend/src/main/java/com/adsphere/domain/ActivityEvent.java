package com.adsphere.domain;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(
    name = "activity_events",
    indexes = {
      @Index(name = "idx_activity_timestamp", columnList = "timestamp"),
      @Index(name = "idx_activity_entity_type", columnList = "entity_type"),
      @Index(name = "idx_activity_action", columnList = "action"),
      @Index(name = "idx_activity_actor", columnList = "actor")
    })
public class ActivityEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Action action;

  @Enumerated(EnumType.STRING)
  @Column(name = "entity_type", nullable = false, length = 20)
  private EntityType entityType;

  @Column(nullable = false)
  private Long entityId;

  @Column(nullable = false)
  private String entityName;

  @Column(nullable = false)
  private String actor;

  @Column(nullable = false, length = 500)
  private String summary;

  @Column(nullable = false, updatable = false)
  private Instant timestamp;

  protected ActivityEvent() {}

  public ActivityEvent(
      Action action,
      EntityType entityType,
      Long entityId,
      String entityName,
      String actor,
      String summary) {
    this.action = action;
    this.entityType = entityType;
    this.entityId = entityId;
    this.entityName = entityName;
    this.actor = actor;
    this.summary = summary;
    this.timestamp = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public Action getAction() {
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

  public String getActor() {
    return actor;
  }

  public String getSummary() {
    return summary;
  }

  public Instant getTimestamp() {
    return timestamp;
  }
}
