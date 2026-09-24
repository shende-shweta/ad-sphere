package com.adsphere.service;

import com.adsphere.domain.ActivityAction;
import com.adsphere.domain.Audience;
import com.adsphere.domain.AudienceStatus;
import com.adsphere.domain.AudienceType;
import com.adsphere.domain.EntityType;
import com.adsphere.dto.AudienceResponse;
import com.adsphere.dto.PageResponse;
import com.adsphere.repository.AudienceRepository;
import com.adsphere.repository.Specs;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AudienceService {

  private final AudienceRepository audiences;
  private final ActivityService activityService;

  public AudienceService(AudienceRepository audiences, ActivityService activityService) {
    this.audiences = audiences;
    this.activityService = activityService;
  }

  @Transactional(readOnly = true)
  public PageResponse<AudienceResponse> search(
      String query, AudienceType type, AudienceStatus status, Pageable pageable) {
    Specification<Audience> spec =
        Specification.where(Specs.<Audience>containsText(query, "name", "description"))
            .and(Specs.equalTo("type", type))
            .and(Specs.equalTo("status", status));
    return PageResponse.of(audiences.findAll(spec, pageable), AudienceResponse::from);
  }

  public AudienceResponse updateStatus(Long id, AudienceStatus status) {
    Audience audience =
        audiences.findById(id).orElseThrow(() -> new NotFoundException("Audience", id));
    audience.setStatus(status);
    activityService.record(
        ActivityAction.STATUS_CHANGED,
        EntityType.AUDIENCE,
        audience.getId(),
        audience.getName(),
        "Audience '" + audience.getName() + "' status changed to " + status);
    return AudienceResponse.from(audience);
  }
}
