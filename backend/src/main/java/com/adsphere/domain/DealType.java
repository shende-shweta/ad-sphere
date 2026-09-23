package com.adsphere.domain;

public enum DealType implements Labeled {
  RTB("RTB"),
  PROGRAMMATIC("Programmatic"),
  PREFERRED("Preferred Deal"),
  PRIVATE_MARKETPLACE("Private Marketplace");

  private final String label;

  DealType(String label) {
    this.label = label;
  }

  @Override
  public String getLabel() {
    return label;
  }
}
