package com.adsphere.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class ProfileUpdateRequest {
  @NotBlank(message = "Full name is required")
  @Size(max = 120, message = "Full name must be at most 120 characters")
  public String fullName;

  @NotBlank(message = "Email is required")
  @Email(message = "Enter a valid email address")
  @Size(max = 160)
  public String email;

  @Pattern(regexp = "^$|^\\+?[0-9 ()-]{7,20}$", message = "Enter a valid phone number")
  public String phone;

  @Size(max = 120, message = "Job title must be at most 120 characters")
  public String jobTitle;

  @Size(max = 64)
  public String timezone;
}
