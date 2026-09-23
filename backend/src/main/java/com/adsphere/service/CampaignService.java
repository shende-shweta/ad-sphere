package com.adsphere.service;

import com.adsphere.domain.Campaign;
import com.adsphere.domain.Placement;
import com.adsphere.dto.CampaignRequest;
import com.adsphere.dto.CampaignResponse;
import com.adsphere.dto.PageResponse;
import com.adsphere.repository.CampaignRepository;
import com.adsphere.repository.PlacementRepository;
import com.adsphere.repository.Specs;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CampaignService {

  private final CampaignRepository campaigns;
  private final PlacementRepository placements;
  private final PlacementService placementService;

  public CampaignService(
      CampaignRepository campaigns,
      PlacementRepository placements,
      PlacementService placementService) {
    this.campaigns = campaigns;
    this.placements = placements;
    this.placementService = placementService;
  }

  @Transactional(readOnly = true)
  public PageResponse<CampaignResponse> search(CampaignFilter filter, Pageable pageable) {
    Specification<Campaign> spec =
        Specification.where(Specs.<Campaign>equalTo("status", filter.status))
            .and(Specs.equalTo("objective", filter.objective))
            .and(Specs.containsText(filter.name, "name", "code"));
    // Date range filter keeps campaigns whose flight overlaps [from, to].
    if (filter.from != null) {
      spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("endDate"), filter.from));
    }
    if (filter.to != null) {
      spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("startDate"), filter.to));
    }
    return PageResponse.of(campaigns.findAll(spec, pageable), CampaignResponse::from);
  }

  @Transactional(readOnly = true)
  public CampaignResponse get(Long id) {
    return CampaignResponse.from(find(id));
  }

  public CampaignResponse create(CampaignRequest request) {
    if (campaigns.existsByNameIgnoreCase(request.name.trim())) {
      throw BusinessRuleException.field("name", "A campaign with this name already exists");
    }
    Campaign campaign = new Campaign();
    apply(campaign, request);
    campaign = campaigns.save(campaign);
    campaign.setCode(String.format("CMP%03d", campaign.getId()));
    return CampaignResponse.from(campaign);
  }

  public CampaignResponse update(Long id, CampaignRequest request) {
    Campaign campaign = find(id);
    if (campaigns.existsByNameIgnoreCaseAndIdNot(request.name.trim(), id)) {
      throw BusinessRuleException.field("name", "A campaign with this name already exists");
    }
    apply(campaign, request);
    return CampaignResponse.from(campaign);
  }

  public void delete(Long id) {
    campaigns.delete(find(id));
  }

  private void apply(Campaign campaign, CampaignRequest request) {
    campaign.setName(request.name.trim());
    campaign.setObjective(request.objective);
    campaign.setStatus(request.status);
    campaign.setStartDate(request.startDate);
    campaign.setEndDate(request.endDate);
    campaign.setBudget(request.budget);
    campaign.setDescription(request.description);

    Set<Placement> assigned = new LinkedHashSet<>();
    List<Long> ids = request.placementIds == null ? List.of() : request.placementIds;
    if (!ids.isEmpty()) {
      List<Placement> found = placements.findAllById(ids);
      Set<Long> distinct = new LinkedHashSet<>(ids);
      if (found.size() != distinct.size()) {
        throw BusinessRuleException.field("placementIds", "One or more placements do not exist");
      }
      assigned.addAll(found);
    }
    if (request.newPlacement != null) {
      assigned.add(placementService.createEntity(request.newPlacement));
    }
    campaign.setPlacements(assigned);
  }

  private Campaign find(Long id) {
    return campaigns.findById(id).orElseThrow(() -> new NotFoundException("Campaign", id));
  }
}
