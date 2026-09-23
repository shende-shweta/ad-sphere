package com.adsphere.service;

import com.adsphere.domain.AppSettings;
import com.adsphere.dto.SettingsDto;
import com.adsphere.repository.AppSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SettingsService {

  private final AppSettingsRepository repository;

  public SettingsService(AppSettingsRepository repository) {
    this.repository = repository;
  }

  public SettingsDto get() {
    return SettingsDto.from(current());
  }

  public SettingsDto update(SettingsDto dto) {
    AppSettings settings = current();
    dto.applyTo(settings);
    return SettingsDto.from(settings);
  }

  AppSettings current() {
    return repository.findFirstByOrderByIdAsc().orElseGet(() -> repository.save(new AppSettings()));
  }
}
