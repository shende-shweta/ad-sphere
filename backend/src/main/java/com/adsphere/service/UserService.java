package com.adsphere.service;

import com.adsphere.domain.AppUser;
import com.adsphere.dto.PasswordChangeRequest;
import com.adsphere.dto.ProfileUpdateRequest;
import com.adsphere.dto.UserResponse;
import com.adsphere.repository.AppUserRepository;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

  private final AppUserRepository users;
  private final PasswordEncoder passwordEncoder;
  private final SettingsService settingsService;

  public UserService(
      AppUserRepository users, PasswordEncoder passwordEncoder, SettingsService settingsService) {
    this.users = users;
    this.passwordEncoder = passwordEncoder;
    this.settingsService = settingsService;
  }

  @Transactional(readOnly = true)
  public UserResponse get(String username) {
    return UserResponse.from(find(username));
  }

  public void recordLogin(String username) {
    find(username).setLastLoginAt(Instant.now());
  }

  public UserResponse updateProfile(String username, ProfileUpdateRequest request) {
    AppUser user = find(username);
    if (users.existsByEmailIgnoreCaseAndIdNot(request.email.trim(), user.getId())) {
      throw BusinessRuleException.field("email", "This email is already used by another account");
    }
    user.setFullName(request.fullName.trim());
    user.setEmail(request.email.trim().toLowerCase());
    user.setPhone(request.phone);
    user.setJobTitle(request.jobTitle);
    user.setTimezone(request.timezone);
    return UserResponse.from(user);
  }

  public void changePassword(String username, PasswordChangeRequest request) {
    AppUser user = find(username);
    if (!passwordEncoder.matches(request.currentPassword, user.getPasswordHash())) {
      throw BusinessRuleException.field("currentPassword", "Current password is incorrect");
    }
    int minLength = settingsService.current().getPasswordMinLength();
    if (request.newPassword.length() < minLength) {
      throw BusinessRuleException.field(
          "newPassword", "Password must be at least " + minLength + " characters");
    }
    if (!request.newPassword.matches(".*[A-Za-z].*") || !request.newPassword.matches(".*\\d.*")) {
      throw BusinessRuleException.field(
          "newPassword", "Password must contain at least one letter and one number");
    }
    if (passwordEncoder.matches(request.newPassword, user.getPasswordHash())) {
      throw BusinessRuleException.field(
          "newPassword", "New password must differ from the current password");
    }
    user.setPasswordHash(passwordEncoder.encode(request.newPassword));
  }

  private AppUser find(String username) {
    return users
        .findByUsernameIgnoreCase(username)
        .orElseThrow(() -> new NotFoundException("User", username));
  }
}
