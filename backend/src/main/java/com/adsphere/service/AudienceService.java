package com.adsphere.service;

import com.adsphere.domain.Audience;
import com.adsphere.domain.AudienceStatus;
import com.adsphere.domain.AudienceType;
import com.adsphere.dto.AudienceRequest;
import com.adsphere.dto.AudienceResponse;
import com.adsphere.dto.PageResponse;
import com.adsphere.repository.AudienceRepository;
import com.adsphere.repository.PlacementRepository;
import com.adsphere.repository.Specs;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AudienceService {

  private final AudienceRepository audiences;
  private final PlacementRepository placements;

  public AudienceService(AudienceRepository audiences, PlacementRepository placements) {
    this.audiences = audiences;
    this.placements = placements;
  }

  @Transactional(readOnly = true)
  public PageResponse<AudienceResponse> search(
      String query, AudienceType type, AudienceStatus status, Pageable pageable) {
    Specification<Audience> spec =
        Specification.where(Specs.<Audience>containsText(query, "name", "description"))
            .and(Specs.equalTo("type", type))
            .and(Specs.equalTo("status", status));
    Page<Audience> page = audiences.findAll(spec, pageable);
    List<Long> ids =
        page.getContent().stream().map(Audience::getId).collect(Collectors.toList());
    Map<Long, Long> counts = batchPlacementCounts(ids);
    return PageResponse.of(
        page, a -> AudienceResponse.from(a, counts.getOrDefault(a.getId(), 0L)));
  }

  @Transactional(readOnly = true)
  public AudienceResponse get(Long id) {
    Audience audience =
        audiences.findById(id).orElseThrow(() -> new NotFoundException("Audience", id));
    long count = placements.countByAudienceId(id);
    return AudienceResponse.from(audience, count);
  }

  public AudienceResponse create(AudienceRequest request) {
    String trimmedName = request.name.trim();
    String description = blankToNull(request.description);

    if (audiences.existsByNameIgnoreCase(trimmedName)) {
      throw BusinessRuleException.field(
          "name", "An audience with this name already exists");
    }

    AudienceStatus effectiveStatus = request.status;
    if (effectiveStatus != null && effectiveStatus != AudienceStatus.ACTIVE) {
      requireAdmin();
    }
    if (effectiveStatus == null) {
      effectiveStatus = AudienceStatus.ACTIVE;
    }

    Audience audience =
        new Audience(trimmedName, request.type, description, effectiveStatus, request.estimatedSize);
    try {
      audience = audiences.save(audience);
    } catch (DataIntegrityViolationException e) {
      throw BusinessRuleException.field(
          "name", "An audience with this name already exists");
    }
    return AudienceResponse.from(audience, 0L);
  }

  public AudienceResponse update(Long id, AudienceRequest request) {
    Audience audience =
        audiences.findById(id).orElseThrow(() -> new NotFoundException("Audience", id));

    if (request.version == null || !request.version.equals(audience.getVersion())) {
      throw BusinessRuleException.conflict(
          "This record was changed by someone else. Reload and retry.");
    }

    String trimmedName = request.name.trim();
    String description = blankToNull(request.description);

    if (audiences.existsByNameIgnoreCaseAndIdNot(trimmedName, id)) {
      throw BusinessRuleException.field(
          "name", "An audience with this name already exists");
    }

    if (request.status != null && request.status != audience.getStatus()) {
      requireAdmin();
      audience.setStatus(request.status);
    }

    audience.setName(trimmedName);
    audience.setType(request.type);
    audience.setDescription(description);
    audience.setEstimatedSize(request.estimatedSize);

    try {
      audience = audiences.save(audience);
    } catch (DataIntegrityViolationException e) {
      throw BusinessRuleException.field(
          "name", "An audience with this name already exists");
    }
    long count = placements.countByAudienceId(id);
    return AudienceResponse.from(audience, count);
  }

  public AudienceResponse updateStatus(Long id, AudienceStatus status) {
    Audience audience =
        audiences.findById(id).orElseThrow(() -> new NotFoundException("Audience", id));
    audience.setStatus(status);
    long count = placements.countByAudienceId(id);
    return AudienceResponse.from(audience, count);
  }

  private Map<Long, Long> batchPlacementCounts(List<Long> audienceIds) {
    if (audienceIds.isEmpty()) {
      return Collections.emptyMap();
    }
    return placements.countByAudienceIds(audienceIds).stream()
        .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
  }

  private static String blankToNull(String value) {
    return value != null && value.trim().isEmpty() ? null : value;
  }

  private static void requireAdmin() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    boolean isAdmin =
        auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch("ROLE_ADMIN"::equals);
    if (!isAdmin) {
      throw new AccessDeniedException("You do not have permission to do this");
    }
  }
}
