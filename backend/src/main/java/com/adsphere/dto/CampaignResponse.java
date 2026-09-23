package com.adsphere.dto;

import com.adsphere.domain.Campaign;
import com.adsphere.domain.CampaignStatus;
import com.adsphere.domain.Objective;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CampaignResponse {
  public final Long id;
  public final String code;
  public final String name;
  public final Objective objective;
  public final CampaignStatus status;
  public final LocalDate startDate;
  public final LocalDate endDate;
  public final BigDecimal budget;
  public final String description;
  public final List<PlacementSummary> placements;
  public final Instant createdAt;
  public final Instant updatedAt;

  private CampaignResponse(Campaign c) {
    this.id = c.getId();
    this.code = c.getCode();
    this.name = c.getName();
    this.objective = c.getObjective();
    this.status = c.getStatus();
    this.startDate = c.getStartDate();
    this.endDate = c.getEndDate();
    this.budget = c.getBudget();
    this.description = c.getDescription();
    this.placements =
        c.getPlacements().stream()
            .sorted(Comparator.comparing(p -> p.getId()))
            .map(p -> new PlacementSummary(p.getId(), p.getName()))
            .collect(Collectors.toList());
    this.createdAt = c.getCreatedAt();
    this.updatedAt = c.getUpdatedAt();
  }

  public static CampaignResponse from(Campaign campaign) {
    return new CampaignResponse(campaign);
  }

  public static class PlacementSummary {
    public final Long id;
    public final String name;

    public PlacementSummary(Long id, String name) {
      this.id = id;
      this.name = name;
    }
  }
}
