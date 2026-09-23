package com.adsphere.domain;

public enum AdPosition implements Labeled {
  ABOVE_THE_FOLD("Above the Fold"),
  BELOW_THE_FOLD("Below the Fold"),
  HEADER("Header"),
  FOOTER("Footer"),
  SIDEBAR("Sidebar"),
  IN_CONTENT("In-Content"),
  PRE_ROLL("Pre-Roll"),
  MID_ROLL("Mid-Roll"),
  POST_ROLL("Post-Roll");

  private final String label;

  AdPosition(String label) {
    this.label = label;
  }

  @Override
  public String getLabel() {
    return label;
  }
}
