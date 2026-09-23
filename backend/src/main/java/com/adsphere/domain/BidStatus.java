package com.adsphere.domain;

public enum BidStatus implements Labeled {
  WON("Won"),
  REJECTED("Rejected");

  private final String label;

  BidStatus(String label) {
    this.label = label;
  }

  @Override
  public String getLabel() {
    return label;
  }
}
