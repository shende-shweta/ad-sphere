package com.adsphere.domain;

public enum AudienceStatus implements Labeled {
  ACTIVE("Active"),
  INACTIVE("Inactive");

  private final String label;

  AudienceStatus(String label) {
    this.label = label;
  }

  @Override
  public String getLabel() {
    return label;
  }
}
