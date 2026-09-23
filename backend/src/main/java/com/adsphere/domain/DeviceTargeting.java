package com.adsphere.domain;

public enum DeviceTargeting implements Labeled {
  ALL_DEVICES("All Devices"),
  DESKTOP("Desktop"),
  MOBILE("Mobile"),
  TABLET("Tablet"),
  CONNECTED_TV("Connected TV");

  private final String label;

  DeviceTargeting(String label) {
    this.label = label;
  }

  @Override
  public String getLabel() {
    return label;
  }
}
