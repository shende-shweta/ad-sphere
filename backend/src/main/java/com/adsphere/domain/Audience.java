package com.adsphere.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Entity
@Table(name = "audiences")
public class Audience extends BaseEntity {

  @Column(nullable = false, unique = true, length = 120)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 24)
  private AudienceType type;

  @Column(length = 500)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private AudienceStatus status;

  /** Estimated number of reachable users. */
  @Column(nullable = false)
  private long estimatedSize;

  public Audience() {}

  public Audience(
      String name, AudienceType type, String description, AudienceStatus status, long size) {
    this.name = name;
    this.type = type;
    this.description = description;
    this.status = status;
    this.estimatedSize = size;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public AudienceType getType() {
    return type;
  }

  public void setType(AudienceType type) {
    this.type = type;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public AudienceStatus getStatus() {
    return status;
  }

  public void setStatus(AudienceStatus status) {
    this.status = status;
  }

  public long getEstimatedSize() {
    return estimatedSize;
  }

  public void setEstimatedSize(long estimatedSize) {
    this.estimatedSize = estimatedSize;
  }
}
