package com.adsphere.domain;

public enum Role implements Labeled {
  ADMIN("Administrator"),
  MANAGER("Campaign Manager"),
  VIEWER("Viewer");

  private final String label;

  Role(String label) {
    this.label = label;
  }

  @Override
  public String getLabel() {
    return label;
  }
}
