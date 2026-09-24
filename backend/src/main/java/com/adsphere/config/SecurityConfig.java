package com.adsphere.config;

import com.adsphere.security.AppUserDetailsService;
import com.adsphere.security.JsonAuthEntryPoint;
import com.adsphere.security.JwtAuthenticationFilter;
import com.adsphere.security.JwtTokenProvider;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final AppUserDetailsService userDetailsService;
  private final JwtTokenProvider tokens;
  private final JsonAuthEntryPoint entryPoint;
  private final List<String> allowedOrigins;

  public SecurityConfig(
      AppUserDetailsService userDetailsService,
      JwtTokenProvider tokens,
      JsonAuthEntryPoint entryPoint,
      @Value("${app.cors.allowed-origins:}") String[] allowedOrigins) {
    this.userDetailsService = userDetailsService;
    this.tokens = tokens;
    this.entryPoint = entryPoint;
    this.allowedOrigins = Arrays.asList(allowedOrigins);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(10);
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.cors()
        .and()
        // Stateless bearer-token API: no cookies, so CSRF protection is not applicable.
        .csrf()
        .disable()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .exceptionHandling()
        .authenticationEntryPoint(entryPoint)
        .accessDeniedHandler(entryPoint)
        .and()
        .headers()
        .contentSecurityPolicy("default-src 'self'; img-src 'self' data:; frame-ancestors 'none'")
        .and()
        .frameOptions()
        .deny()
        .and()
        .authorizeRequests()
        .antMatchers(HttpMethod.POST, "/api/auth/login")
        .permitAll()
        .antMatchers("/actuator/health")
        .permitAll()
        // Role rules are enforced here (before body validation) and again with @PreAuthorize.
        .antMatchers(HttpMethod.DELETE, "/api/campaigns/**")
        .hasRole("ADMIN")
        .antMatchers(HttpMethod.POST, "/api/campaigns/**", "/api/placements/**", "/api/deals/**")
        .hasAnyRole("ADMIN", "MANAGER")
        .antMatchers(HttpMethod.PUT, "/api/campaigns/**")
        .hasAnyRole("ADMIN", "MANAGER")
        .antMatchers(HttpMethod.PUT, "/api/settings/**")
        .hasRole("ADMIN")
        .antMatchers(HttpMethod.PATCH, "/api/audiences/**")
        .hasRole("ADMIN")
        .antMatchers(HttpMethod.GET, "/api/activity/**")
        .authenticated()
        .antMatchers("/api/activity/**")
        .denyAll()
        .antMatchers("/api/**")
        .authenticated()
        // Static frontend assets (when bundled into the jar).
        .anyRequest()
        .permitAll()
        .and()
        .addFilterBefore(
            new JwtAuthenticationFilter(tokens, userDetailsService),
            UsernamePasswordAuthenticationFilter.class);
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(allowedOrigins);
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
    config.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
  }
}
