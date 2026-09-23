package com.adsphere.web;

import com.adsphere.dto.LoginRequest;
import com.adsphere.dto.LoginResponse;
import com.adsphere.dto.UserResponse;
import com.adsphere.security.JwtTokenProvider;
import com.adsphere.service.UserService;
import java.security.Principal;
import javax.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider tokens;
  private final UserService users;

  public AuthController(
      AuthenticationManager authenticationManager, JwtTokenProvider tokens, UserService users) {
    this.authenticationManager = authenticationManager;
    this.tokens = tokens;
    this.users = users;
  }

  @PostMapping("/login")
  public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    Authentication auth =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username.trim(), request.password));
    String role =
        auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .findFirst()
            .orElse("ROLE_VIEWER")
            .substring("ROLE_".length());
    JwtTokenProvider.IssuedToken token = tokens.issue(auth.getName(), role);
    users.recordLogin(auth.getName());
    return new LoginResponse(token.token, token.expiresAt, users.get(auth.getName()));
  }

  @GetMapping("/me")
  public UserResponse me(Principal principal) {
    return users.get(principal.getName());
  }
}
