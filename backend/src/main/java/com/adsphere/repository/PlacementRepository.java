package com.adsphere.repository;

import com.adsphere.domain.Placement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PlacementRepository
    extends JpaRepository<Placement, Long>, JpaSpecificationExecutor<Placement> {}
