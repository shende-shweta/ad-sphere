package com.adsphere.web;

import com.adsphere.domain.CampaignStatus;
import com.adsphere.domain.Objective;
import com.adsphere.dto.CampaignRequest;
import com.adsphere.dto.CampaignResponse;
import com.adsphere.dto.PageResponse;
import com.adsphere.service.CampaignFilter;
import com.adsphere.service.CampaignService;
import java.net.URI;
import java.time.LocalDate;
import javax.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

  private final CampaignService service;

  public CampaignController(CampaignService service) {
    this.service = service;
  }

  @GetMapping
  public PageResponse<CampaignResponse> list(
      @RequestParam(required = false) CampaignStatus status,
      @RequestParam(required = false) Objective objective,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
    CampaignFilter filter = new CampaignFilter();
    filter.status = status;
    filter.objective = objective;
    filter.name = name;
    filter.from = from;
    filter.to = to;
    return service.search(filter, pageable);
  }

  @GetMapping("/{id}")
  public CampaignResponse get(@PathVariable Long id) {
    return service.get(id);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
  public ResponseEntity<CampaignResponse> create(@Valid @RequestBody CampaignRequest request) {
    CampaignResponse created = service.create(request);
    return ResponseEntity.created(URI.create("/api/campaigns/" + created.id)).body(created);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
  public CampaignResponse update(
      @PathVariable Long id, @Valid @RequestBody CampaignRequest request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
