package com.adsphere.repository;

import com.adsphere.domain.AppSettings;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppSettingsRepository extends JpaRepository<AppSettings, Long> {

  Optional<AppSettings> findFirstByOrderByIdAsc();
}
