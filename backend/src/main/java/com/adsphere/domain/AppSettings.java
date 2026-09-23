package com.adsphere.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/** Platform-wide settings. A single row is kept. */
@Entity
@Table(name = "app_settings")
public class AppSettings extends BaseEntity {

  // General
  @Column(nullable = false, length = 80)
  private String platformName = "AdSphere";

  @Column(nullable = false, length = 64)
  private String timezone = "UTC";

  @Column(nullable = false, length = 16)
  private String language = "en-US";

  @Column(nullable = false, length = 8)
  private String currency = "USD";

  // Integrations
  private boolean analyticsIntegration;
  private boolean crmIntegration;
  private boolean slackIntegration;

  @Column(length = 500)
  private String webhookUrl;

  // Notifications
  private boolean emailNotifications = true;
  private boolean systemNotifications = true;
  private boolean budgetAlerts = true;
  private boolean weeklyReport;

  // Security
  private boolean twoFactorRequired;
  private int sessionTimeoutMinutes = 60;
  private int passwordMinLength = 8;

  public String getPlatformName() {
    return platformName;
  }

  public void setPlatformName(String platformName) {
    this.platformName = platformName;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public boolean isAnalyticsIntegration() {
    return analyticsIntegration;
  }

  public void setAnalyticsIntegration(boolean analyticsIntegration) {
    this.analyticsIntegration = analyticsIntegration;
  }

  public boolean isCrmIntegration() {
    return crmIntegration;
  }

  public void setCrmIntegration(boolean crmIntegration) {
    this.crmIntegration = crmIntegration;
  }

  public boolean isSlackIntegration() {
    return slackIntegration;
  }

  public void setSlackIntegration(boolean slackIntegration) {
    this.slackIntegration = slackIntegration;
  }

  public String getWebhookUrl() {
    return webhookUrl;
  }

  public void setWebhookUrl(String webhookUrl) {
    this.webhookUrl = webhookUrl;
  }

  public boolean isEmailNotifications() {
    return emailNotifications;
  }

  public void setEmailNotifications(boolean emailNotifications) {
    this.emailNotifications = emailNotifications;
  }

  public boolean isSystemNotifications() {
    return systemNotifications;
  }

  public void setSystemNotifications(boolean systemNotifications) {
    this.systemNotifications = systemNotifications;
  }

  public boolean isBudgetAlerts() {
    return budgetAlerts;
  }

  public void setBudgetAlerts(boolean budgetAlerts) {
    this.budgetAlerts = budgetAlerts;
  }

  public boolean isWeeklyReport() {
    return weeklyReport;
  }

  public void setWeeklyReport(boolean weeklyReport) {
    this.weeklyReport = weeklyReport;
  }

  public boolean isTwoFactorRequired() {
    return twoFactorRequired;
  }

  public void setTwoFactorRequired(boolean twoFactorRequired) {
    this.twoFactorRequired = twoFactorRequired;
  }

  public int getSessionTimeoutMinutes() {
    return sessionTimeoutMinutes;
  }

  public void setSessionTimeoutMinutes(int sessionTimeoutMinutes) {
    this.sessionTimeoutMinutes = sessionTimeoutMinutes;
  }

  public int getPasswordMinLength() {
    return passwordMinLength;
  }

  public void setPasswordMinLength(int passwordMinLength) {
    this.passwordMinLength = passwordMinLength;
  }
}
