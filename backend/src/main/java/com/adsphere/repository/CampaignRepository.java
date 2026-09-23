package com.adsphere.repository;

import com.adsphere.domain.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CampaignRepository
    extends JpaRepository<Campaign, Long>, JpaSpecificationExecutor<Campaign> {

  boolean existsByNameIgnoreCase(String name);

  boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
