package com.adsphere.dto;

import java.time.Instant;

public class LoginResponse {
  public final String token;
  public final Instant expiresAt;
  public final UserResponse user;

  public LoginResponse(String token, Instant expiresAt, UserResponse user) {
    this.token = token;
    this.expiresAt = expiresAt;
    this.user = user;
  }
}
