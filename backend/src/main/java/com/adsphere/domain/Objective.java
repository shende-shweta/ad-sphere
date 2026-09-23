package com.adsphere.domain;

public enum Objective implements Labeled {
  BRAND_AWARENESS("Brand Awareness"),
  CONVERSIONS("Conversions"),
  TRAFFIC("Traffic"),
  REACH("Reach"),
  VIDEO_VIEWS("Video Views"),
  APP_INSTALLS("App Installs");

  private final String label;

  Objective(String label) {
    this.label = label;
  }

  @Override
  public String getLabel() {
    return label;
  }
}
