package com.adsphere.repository;

import com.adsphere.domain.Audience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AudienceRepository
    extends JpaRepository<Audience, Long>, JpaSpecificationExecutor<Audience> {

  boolean existsByNameIgnoreCase(String name);

  boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
