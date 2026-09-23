package com.adsphere.domain;

public enum DealStatus implements Labeled {
  ACTIVE("Active"),
  PAUSED("Paused"),
  SOLD("Sold");

  private final String label;

  DealStatus(String label) {
    this.label = label;
  }

  @Override
  public String getLabel() {
    return label;
  }
}
