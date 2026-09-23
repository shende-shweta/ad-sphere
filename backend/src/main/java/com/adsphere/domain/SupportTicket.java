package com.adsphere.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "support_tickets")
public class SupportTicket extends BaseEntity {

  @Column(nullable = false, length = 64)
  private String requester;

  @Column(nullable = false, length = 160)
  private String subject;

  @Column(nullable = false, length = 4000)
  private String message;

  @Column(nullable = false, length = 16)
  private String status = "OPEN";

  public SupportTicket() {}

  public SupportTicket(String requester, String subject, String message) {
    this.requester = requester;
    this.subject = subject;
    this.message = message;
  }

  public String getRequester() {
    return requester;
  }

  public String getSubject() {
    return subject;
  }

  public String getMessage() {
    return message;
  }

  public String getStatus() {
    return status;
  }
}
