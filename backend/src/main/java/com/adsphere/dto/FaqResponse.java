package com.adsphere.dto;

public class FaqResponse {
  public final String category;
  public final String question;
  public final String answer;

  public FaqResponse(String category, String question, String answer) {
    this.category = category;
    this.question = question;
    this.answer = answer;
  }
}
