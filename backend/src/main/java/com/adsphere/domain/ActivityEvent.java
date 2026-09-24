package com.adsphere.domain;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "activity_event")
public class ActivityEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String actor;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Action action;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private EntityType entityType;

  @Column(nullable = false)
  private Long entityId;

  @Column(nullable = false)
  private String entityName;

  @Column(nullable = false, length = 500)
  private String summary;

  @Column(nullable = false, updatable = false)
  private Instant timestamp;

  protected ActivityEvent() {}

  public ActivityEvent(String actor, Action action, EntityType entityType,
      Long entityId, String entityName, String summary) {
    this.actor = actor;
    this.action = action;
    this.entityType = entityType;
    this.entityId = entityId;
    this.entityName = entityName;
    this.summary = summary;
    this.timestamp = Instant.now();
  }

  public Long getId() { return id; }
  public String getActor() { return actor; }
  public Action getAction() { return action; }
  public EntityType getEntityType() { return entityType; }
  public Long getEntityId() { return entityId; }
  public String getEntityName() { return entityName; }
  public String getSummary() { return summary; }
  public Instant getTimestamp() { return timestamp; }

  public enum Action {
    CREATED, UPDATED, DELETED, STATUS_CHANGED, BID_PLACED
  }

  public enum EntityType {
    CAMPAIGN, PLACEMENT, AUDIENCE, DEAL
  }
}
