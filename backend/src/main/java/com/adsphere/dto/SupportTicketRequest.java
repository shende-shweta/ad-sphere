package com.adsphere.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class SupportTicketRequest {
  @NotBlank(message = "Subject is required")
  @Size(max = 160, message = "Subject must be at most 160 characters")
  public String subject;

  @NotBlank(message = "Message is required")
  @Size(min = 10, max = 4000, message = "Message must be between 10 and 4000 characters")
  public String message;
}
