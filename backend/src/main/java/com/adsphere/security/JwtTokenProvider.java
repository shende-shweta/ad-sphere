package com.adsphere.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  private final JwtProperties properties;
  private final byte[] key;

  public JwtTokenProvider(JwtProperties properties) {
    this.properties = properties;
    this.key = properties.getSecret().getBytes(StandardCharsets.UTF_8);
  }

  public IssuedToken issue(String username, String role) {
    Instant now = Instant.now();
    Instant expiresAt = now.plus(properties.getExpirationMinutes(), ChronoUnit.MINUTES);
    String token =
        Jwts.builder()
            .setIssuer(properties.getIssuer())
            .setSubject(username)
            .claim("role", role)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiresAt))
            .signWith(SignatureAlgorithm.HS256, key)
            .compact();
    return new IssuedToken(token, expiresAt);
  }

  /** Returns the username for a valid, unexpired token signed by this service. */
  public Optional<String> validate(String token) {
    try {
      Claims claims =
          Jwts.parser()
              .requireIssuer(properties.getIssuer())
              .setSigningKey(key)
              .parseClaimsJws(token)
              .getBody();
      return Optional.ofNullable(claims.getSubject());
    } catch (JwtException | IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  public static final class IssuedToken {
    public final String token;
    public final Instant expiresAt;

    IssuedToken(String token, Instant expiresAt) {
      this.token = token;
      this.expiresAt = expiresAt;
    }
  }
}
