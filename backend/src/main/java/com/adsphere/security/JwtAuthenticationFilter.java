package com.adsphere.security;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/** Authenticates requests carrying {@code Authorization: Bearer <jwt>}. */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String PREFIX = "Bearer ";

  private final JwtTokenProvider tokens;
  private final UserDetailsService userDetailsService;

  public JwtAuthenticationFilter(JwtTokenProvider tokens, UserDetailsService userDetailsService) {
    this.tokens = tokens;
    this.userDetailsService = userDetailsService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith(PREFIX)) {
      tokens
          .validate(header.substring(PREFIX.length()).trim())
          .ifPresent(username -> authenticate(username, request));
    }
    chain.doFilter(request, response);
  }

  private void authenticate(String username, HttpServletRequest request) {
    try {
      UserDetails user = userDetailsService.loadUserByUsername(username);
      if (!user.isEnabled()) {
        return;
      }
      UsernamePasswordAuthenticationToken auth =
          new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
      auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(auth);
    } catch (UsernameNotFoundException e) {
      // Token for a deleted user: leave the request unauthenticated.
    }
  }
}
