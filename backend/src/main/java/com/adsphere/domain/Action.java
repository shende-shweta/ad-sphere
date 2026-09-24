package com.adsphere.domain;

public enum Action implements Labeled {
  CREATED("Created"),
  UPDATED("Updated"),
  DELETED("Deleted"),
  STATUS_CHANGED("Status changed"),
  BID_PLACED("Bid placed");

  private final String label;

  Action(String label) {
    this.label = label;
  }

  @Override
  public String getLabel() {
    return label;
  }
}
