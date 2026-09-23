package com.adsphere.web;

import com.adsphere.domain.AudienceStatus;
import com.adsphere.domain.AudienceType;
import com.adsphere.dto.AudienceResponse;
import com.adsphere.dto.PageResponse;
import com.adsphere.service.AudienceService;
import javax.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audiences")
public class AudienceController {

  private final AudienceService service;

  public AudienceController(AudienceService service) {
    this.service = service;
  }

  @GetMapping
  public PageResponse<AudienceResponse> list(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) AudienceType type,
      @RequestParam(required = false) AudienceStatus status,
      @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
    return service.search(q, type, status, pageable);
  }

  @PatchMapping("/{id}/status")
  @PreAuthorize("hasRole('ADMIN')")
  public AudienceResponse updateStatus(
      @PathVariable Long id, @RequestBody @javax.validation.Valid StatusChange body) {
    return service.updateStatus(id, body.status);
  }

  public static class StatusChange {
    @NotNull(message = "Status is required")
    public AudienceStatus status;
  }
}
