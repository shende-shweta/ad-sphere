package com.adsphere.repository;

import com.adsphere.domain.Bid;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BidRepository extends JpaRepository<Bid, Long> {

  List<Bid> findByDealIdOrderByCreatedAtDesc(Long dealId);
}
