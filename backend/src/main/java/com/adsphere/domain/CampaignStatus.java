package com.adsphere.domain;

public enum CampaignStatus implements Labeled {
  DRAFT("Draft"),
  ACTIVE("Active"),
  PAUSED("Paused"),
  COMPLETED("Completed");

  private final String label;

  CampaignStatus(String label) {
    this.label = label;
  }

  @Override
  public String getLabel() {
    return label;
  }
}
