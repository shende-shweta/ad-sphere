package com.adsphere.web;

import com.adsphere.dto.PageResponse;
import com.adsphere.dto.PlacementRequest;
import com.adsphere.dto.PlacementResponse;
import com.adsphere.service.PlacementService;
import java.net.URI;
import javax.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/placements")
public class PlacementController {

  private final PlacementService service;

  public PlacementController(PlacementService service) {
    this.service = service;
  }

  @GetMapping
  public PageResponse<PlacementResponse> list(
      @RequestParam(required = false) String q,
      @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
          Pageable pageable) {
    return service.search(q, pageable);
  }

  @GetMapping("/{id}")
  public PlacementResponse get(@PathVariable Long id) {
    return service.get(id);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
  public ResponseEntity<PlacementResponse> create(@Valid @RequestBody PlacementRequest request) {
    PlacementResponse created = service.create(request);
    return ResponseEntity.created(URI.create("/api/placements/" + created.id)).body(created);
  }
}
