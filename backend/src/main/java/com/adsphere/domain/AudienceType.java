package com.adsphere.domain;

public enum AudienceType implements Labeled {
  DEMOGRAPHIC("Demographic"),
  INTEREST("Interest"),
  LOCATION("Location"),
  DEVICE("Device"),
  INCOME("Income"),
  BEHAVIORAL("Behavioral");

  private final String label;

  AudienceType(String label) {
    this.label = label;
  }

  @Override
  public String getLabel() {
    return label;
  }
}
