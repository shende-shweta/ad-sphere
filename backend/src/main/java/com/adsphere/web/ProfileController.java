package com.adsphere.web;

import com.adsphere.dto.PasswordChangeRequest;
import com.adsphere.dto.ProfileUpdateRequest;
import com.adsphere.dto.UserResponse;
import com.adsphere.service.UserService;
import java.security.Principal;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

  private final UserService users;

  public ProfileController(UserService users) {
    this.users = users;
  }

  @GetMapping
  public UserResponse get(Principal principal) {
    return users.get(principal.getName());
  }

  @PutMapping
  public UserResponse update(
      @Valid @RequestBody ProfileUpdateRequest request, Principal principal) {
    return users.updateProfile(principal.getName(), request);
  }

  @PutMapping("/password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void changePassword(
      @Valid @RequestBody PasswordChangeRequest request, Principal principal) {
    users.changePassword(principal.getName(), request);
  }
}
