package com.adsphere.domain;

public enum VideoTargeting implements Labeled {
  ALL_VIDEO("All Video"),
  IN_STREAM("In-Stream"),
  OUT_STREAM("Out-Stream"),
  IN_BANNER("In-Banner"),
  NO_VIDEO("No Video");

  private final String label;

  VideoTargeting(String label) {
    this.label = label;
  }

  @Override
  public String getLabel() {
    return label;
  }
}
