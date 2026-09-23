package com.adsphere.repository;

import com.adsphere.domain.AppUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

  Optional<AppUser> findByUsernameIgnoreCase(String username);

  boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
