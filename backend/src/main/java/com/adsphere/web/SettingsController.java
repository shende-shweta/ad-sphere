package com.adsphere.web;

import com.adsphere.dto.SettingsDto;
import com.adsphere.service.SettingsService;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

  private final SettingsService service;

  public SettingsController(SettingsService service) {
    this.service = service;
  }

  @GetMapping
  public SettingsDto get() {
    return service.get();
  }

  @PutMapping
  @PreAuthorize("hasRole('ADMIN')")
  public SettingsDto update(@Valid @RequestBody SettingsDto settings) {
    return service.update(settings);
  }
}
