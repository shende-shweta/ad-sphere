package com.adsphere.dto;

import com.adsphere.domain.AppSettings;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class SettingsDto {
  @NotBlank(message = "Platform name is required")
  @Size(max = 80)
  public String platformName;

  @NotBlank(message = "Timezone is required")
  @Size(max = 64)
  public String timezone;

  @NotBlank(message = "Language is required")
  @Size(max = 16)
  public String language;

  @NotBlank(message = "Currency is required")
  @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO code")
  public String currency;

  public boolean analyticsIntegration;
  public boolean crmIntegration;
  public boolean slackIntegration;

  @Size(max = 500)
  @Pattern(regexp = "^$|^https://\\S+$", message = "Webhook URL must start with https://")
  public String webhookUrl;

  public boolean emailNotifications;
  public boolean systemNotifications;
  public boolean budgetAlerts;
  public boolean weeklyReport;

  public boolean twoFactorRequired;

  @Min(value = 5, message = "Session timeout must be at least 5 minutes")
  @Max(value = 1440, message = "Session timeout must be at most 1440 minutes")
  public int sessionTimeoutMinutes;

  @Min(value = 8, message = "Minimum password length must be at least 8")
  @Max(value = 64, message = "Minimum password length must be at most 64")
  public int passwordMinLength;

  public static SettingsDto from(AppSettings s) {
    SettingsDto d = new SettingsDto();
    d.platformName = s.getPlatformName();
    d.timezone = s.getTimezone();
    d.language = s.getLanguage();
    d.currency = s.getCurrency();
    d.analyticsIntegration = s.isAnalyticsIntegration();
    d.crmIntegration = s.isCrmIntegration();
    d.slackIntegration = s.isSlackIntegration();
    d.webhookUrl = s.getWebhookUrl();
    d.emailNotifications = s.isEmailNotifications();
    d.systemNotifications = s.isSystemNotifications();
    d.budgetAlerts = s.isBudgetAlerts();
    d.weeklyReport = s.isWeeklyReport();
    d.twoFactorRequired = s.isTwoFactorRequired();
    d.sessionTimeoutMinutes = s.getSessionTimeoutMinutes();
    d.passwordMinLength = s.getPasswordMinLength();
    return d;
  }

  public void applyTo(AppSettings s) {
    s.setPlatformName(platformName.trim());
    s.setTimezone(timezone);
    s.setLanguage(language);
    s.setCurrency(currency);
    s.setAnalyticsIntegration(analyticsIntegration);
    s.setCrmIntegration(crmIntegration);
    s.setSlackIntegration(slackIntegration);
    s.setWebhookUrl(webhookUrl);
    s.setEmailNotifications(emailNotifications);
    s.setSystemNotifications(systemNotifications);
    s.setBudgetAlerts(budgetAlerts);
    s.setWeeklyReport(weeklyReport);
    s.setTwoFactorRequired(twoFactorRequired);
    s.setSessionTimeoutMinutes(sessionTimeoutMinutes);
    s.setPasswordMinLength(passwordMinLength);
  }
}
