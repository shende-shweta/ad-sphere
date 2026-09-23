package com.adsphere.repository;

import com.adsphere.domain.Deal;
import java.util.Optional;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DealRepository extends JpaRepository<Deal, Long>, JpaSpecificationExecutor<Deal> {

  /** Row-locks the deal so two concurrent winning bids cannot both succeed. */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select d from Deal d where d.id = :id")
  Optional<Deal> findByIdForUpdate(@Param("id") Long id);
}
