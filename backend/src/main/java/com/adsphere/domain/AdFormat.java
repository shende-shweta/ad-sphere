package com.adsphere.domain;

public enum AdFormat implements Labeled {
  DISPLAY_BANNER("Display Banner"),
  VIDEO("Video"),
  NATIVE("Native"),
  RICH_MEDIA("Rich Media"),
  AUDIO("Audio");

  private final String label;

  AdFormat(String label) {
    this.label = label;
  }

  @Override
  public String getLabel() {
    return label;
  }
}
