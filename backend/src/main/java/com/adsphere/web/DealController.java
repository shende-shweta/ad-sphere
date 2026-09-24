package com.adsphere.web;

import com.adsphere.domain.DealStatus;
import com.adsphere.domain.DealType;
import com.adsphere.dto.BidRequest;
import com.adsphere.dto.BidResponse;
import com.adsphere.dto.DealResponse;
import com.adsphere.dto.PageResponse;
import com.adsphere.service.DealService;
import java.security.Principal;
import javax.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deals")
public class DealController {

  private final DealService service;

  public DealController(DealService service) {
    this.service = service;
  }

  @GetMapping
  public PageResponse<DealResponse> list(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) DealType type,
      @RequestParam(required = false) DealStatus status,
      @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
    return service.search(q, type, status, pageable);
  }

  @GetMapping("/{id}")
  public DealResponse get(@PathVariable Long id) {
    return service.get(id);
  }

  @PostMapping("/{id}/bids")
  @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
  public BidResponse bid(
      @PathVariable Long id, @Valid @RequestBody BidRequest request, Principal principal) {
    return service.placeBid(id, request, principal.getName());
  }
}
