package com.adsphere.dto;

import com.adsphere.domain.AppUser;
import com.adsphere.domain.Role;
import java.time.Instant;

public class UserResponse {
  public final Long id;
  public final String username;
  public final String fullName;
  public final String email;
  public final String phone;
  public final String jobTitle;
  public final String timezone;
  public final Role role;
  public final Instant lastLoginAt;
  public final Instant passwordChangedAt;

  private UserResponse(AppUser u) {
    this.id = u.getId();
    this.username = u.getUsername();
    this.fullName = u.getFullName();
    this.email = u.getEmail();
    this.phone = u.getPhone();
    this.jobTitle = u.getJobTitle();
    this.timezone = u.getTimezone();
    this.role = u.getRole();
    this.lastLoginAt = u.getLastLoginAt();
    this.passwordChangedAt = u.getPasswordChangedAt();
  }

  public static UserResponse from(AppUser user) {
    return new UserResponse(user);
  }
}
