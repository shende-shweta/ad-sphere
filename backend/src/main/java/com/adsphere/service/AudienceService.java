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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
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
    Map<Long, Long> counts =
        ids.isEmpty() ? Collections.emptyMap() : countsByAudienceId(ids);
    return PageResponse.of(
        page, a -> AudienceResponse.from(a, counts.getOrDefault(a.getId(), 0L)));
  }

  @Transactional(readOnly = true)
  public AudienceResponse get(Long id) {
    Audience audience = find(id);
    long count = placements.countByAudienceId(id);
    return AudienceResponse.from(audience, count);
  }

  public AudienceResponse create(AudienceRequest request) {
    String trimmedName = request.name.trim();
    if (trimmedName.length() < 3) {
      throw BusinessRuleException.field("name", "Audience name must be 3-120 characters");
    }
    if (audiences.existsByNameIgnoreCase(trimmedName)) {
      throw BusinessRuleException.field(
          "name", "An audience with this name already exists");
    }
    checkStatusPermission(request.status, null);

    Audience audience = new Audience();
    audience.setName(trimmedName);
    audience.setType(request.type);
    audience.setDescription(blankToNull(request.description));
    audience.setEstimatedSize(request.estimatedSize);
    audience.setStatus(request.status != null ? request.status : AudienceStatus.ACTIVE);
    audience = audiences.save(audience);
    return AudienceResponse.from(audience, 0L);
  }

  public AudienceResponse update(Long id, AudienceRequest request) {
    Audience audience = find(id);
    if (request.version == null || !request.version.equals(audience.getVersion())) {
      throw BusinessRuleException.conflict(
          "This record was changed by someone else. Reload and retry.");
    }
    String trimmedName = request.name.trim();
    if (trimmedName.length() < 3) {
      throw BusinessRuleException.field("name", "Audience name must be 3-120 characters");
    }
    if (audiences.existsByNameIgnoreCaseAndIdNot(trimmedName, id)) {
      throw BusinessRuleException.field(
          "name", "An audience with this name already exists");
    }
    checkStatusPermission(request.status, audience.getStatus());

    audience.setName(trimmedName);
    audience.setType(request.type);
    audience.setDescription(blankToNull(request.description));
    audience.setEstimatedSize(request.estimatedSize);
    if (request.status != null) {
      audience.setStatus(request.status);
    }
    audience = audiences.save(audience);
    long count = placements.countByAudienceId(id);
    return AudienceResponse.from(audience, count);
  }

  public AudienceResponse updateStatus(Long id, AudienceStatus status) {
    Audience audience = find(id);
    audience.setStatus(status);
    long count = placements.countByAudienceId(id);
    return AudienceResponse.from(audience, count);
  }

  private Audience find(Long id) {
    return audiences
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Audience", id));
  }

  private Map<Long, Long> countsByAudienceId(List<Long> ids) {
    return placements.countByAudienceIdIn(ids).stream()
        .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
  }

  private void checkStatusPermission(
      AudienceStatus requestedStatus, AudienceStatus currentStatus) {
    if (requestedStatus == null) return;
    boolean statusChanging =
        currentStatus == null
            ? requestedStatus != AudienceStatus.ACTIVE
            : requestedStatus != currentStatus;
    if (!statusChanging) return;
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    boolean isAdmin =
        auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    if (!isAdmin) {
      throw new AccessDeniedException("You do not have permission to do this");
    }
  }

  private static String blankToNull(String value) {
    if (value == null) return null;
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
