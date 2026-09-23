package com.adsphere.domain;

public enum Country implements Labeled {
  US("United States"),
  CA("Canada"),
  GB("United Kingdom"),
  DE("Germany"),
  FR("France"),
  IN("India"),
  JP("Japan"),
  AU("Australia"),
  BR("Brazil"),
  SG("Singapore");

  private final String label;

  Country(String label) {
    this.label = label;
  }

  @Override
  public String getLabel() {
    return label;
  }
}
