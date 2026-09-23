package com.adsphere.repository;

import com.adsphere.domain.Placement;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlacementRepository
    extends JpaRepository<Placement, Long>, JpaSpecificationExecutor<Placement> {

  long countByAudienceId(Long audienceId);

  @Query(
      "SELECT p.audience.id, COUNT(p) FROM Placement p"
          + " WHERE p.audience.id IN :ids GROUP BY p.audience.id")
  List<Object[]> countByAudienceIdIn(@Param("ids") Collection<Long> ids);
}
