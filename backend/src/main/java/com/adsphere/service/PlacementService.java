package com.adsphere.service;

import com.adsphere.domain.ActivityEvent.Action;
import com.adsphere.domain.ActivityEvent.EntityType;
import com.adsphere.domain.Audience;
import com.adsphere.domain.AudienceStatus;
import com.adsphere.domain.Placement;
import com.adsphere.dto.PageResponse;
import com.adsphere.dto.PlacementRequest;
import com.adsphere.dto.PlacementResponse;
import com.adsphere.repository.AudienceRepository;
import com.adsphere.repository.PlacementRepository;
import com.adsphere.repository.Specs;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class PlacementService {

  private final PlacementRepository placements;
  private final AudienceRepository audiences;
  private final ActivityService activityService;

  public PlacementService(PlacementRepository placements, AudienceRepository audiences,
      ActivityService activityService) {
    this.placements = placements;
    this.audiences = audiences;
    this.activityService = activityService;
  }

  @Transactional(readOnly = true)
  public PageResponse<PlacementResponse> search(String query, Pageable pageable) {
    return PageResponse.of(
        placements.findAll(Specs.containsText(query, "name"), pageable), PlacementResponse::from);
  }

  @Transactional(readOnly = true)
  public PlacementResponse get(Long id) {
    return PlacementResponse.from(
        placements.findById(id).orElseThrow(() -> new NotFoundException("Placement", id)));
  }

  public PlacementResponse create(PlacementRequest request) {
    return PlacementResponse.from(createEntity(request));
  }

  Placement createEntity(PlacementRequest request) {
    Audience audience =
        audiences
            .findById(request.audienceId)
            .orElseThrow(
                () -> BusinessRuleException.field("audienceId", "Audience group does not exist"));
    if (audience.getStatus() != AudienceStatus.ACTIVE) {
      throw BusinessRuleException.field("audienceId", "Audience group is not active");
    }
    Placement p = new Placement();
    p.setName(request.name.trim());
    p.setCountry(request.country);
    p.setAudience(audience);
    p.setVideoTargeting(request.videoTargeting);
    p.setTraffic(request.traffic);
    p.setAdPosition(request.adPosition);
    p.setDealType(request.dealType);
    p.setFrequencyCap(StringUtils.hasText(request.frequencyCap) ? request.frequencyCap : null);
    p.setDeviceTargeting(request.deviceTargeting);
    p.setAdFormat(request.adFormat);
    p.setNotes(request.notes);
    p = placements.save(p);
    activityService.record(Action.CREATED, EntityType.PLACEMENT,
        p.getId(), p.getName(), "Created placement \"" + p.getName() + "\"");
    return p;
  }
}
