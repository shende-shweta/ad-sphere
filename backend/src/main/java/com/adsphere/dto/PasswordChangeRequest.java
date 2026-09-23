package com.adsphere.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class PasswordChangeRequest {
  @NotBlank(message = "Current password is required")
  public String currentPassword;

  @NotBlank(message = "New password is required")
  @Size(max = 72, message = "Password must be at most 72 characters")
  public String newPassword;
}
