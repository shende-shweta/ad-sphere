package com.adsphere.domain;

public enum TrafficType implements Labeled {
  APP("App"),
  WEBSITE("Website");

  private final String label;

  TrafficType(String label) {
    this.label = label;
  }

  @Override
  public String getLabel() {
    return label;
  }
}
