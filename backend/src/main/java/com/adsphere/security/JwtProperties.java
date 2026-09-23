package com.adsphere.security;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

  /** HMAC-SHA256 signing secret. Override with APP_JWT_SECRET in every real environment. */
  @NotBlank
  @Size(min = 32, message = "app.jwt.secret must be at least 32 characters")
  private String secret;

  @Min(5)
  private long expirationMinutes = 60;

  private String issuer = "adsphere";

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public long getExpirationMinutes() {
    return expirationMinutes;
  }

  public void setExpirationMinutes(long expirationMinutes) {
    this.expirationMinutes = expirationMinutes;
  }

  public String getIssuer() {
    return issuer;
  }

  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }
}
