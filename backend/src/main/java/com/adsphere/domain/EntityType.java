package com.adsphere.domain;

public enum EntityType implements Labeled {
  CAMPAIGN("Campaign"),
  PLACEMENT("Placement"),
  AUDIENCE("Audience"),
  DEAL("Deal");

  private final String label;

  EntityType(String label) {
    this.label = label;
  }

  @Override
  public String getLabel() {
    return label;
  }
}
