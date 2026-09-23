package com.adsphere.web;

import com.adsphere.domain.AdFormat;
import com.adsphere.domain.AdPosition;
import com.adsphere.domain.AudienceStatus;
import com.adsphere.domain.AudienceType;
import com.adsphere.domain.CampaignStatus;
import com.adsphere.domain.Country;
import com.adsphere.domain.DealStatus;
import com.adsphere.domain.DealType;
import com.adsphere.domain.DeviceTargeting;
import com.adsphere.domain.Labeled;
import com.adsphere.domain.Objective;
import com.adsphere.domain.Role;
import com.adsphere.domain.TrafficType;
import com.adsphere.domain.VideoTargeting;
import com.adsphere.dto.LookupOption;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Exposes enum values and labels so the UI never hard-codes them. */
@RestController
@RequestMapping("/api/lookups")
public class LookupController {

  private static final Map<String, List<LookupOption>> LOOKUPS = new LinkedHashMap<>();

  static {
    put("campaignStatuses", CampaignStatus.values());
    put("objectives", Objective.values());
    put("countries", Country.values());
    put("videoTargeting", VideoTargeting.values());
    put("trafficTypes", TrafficType.values());
    put("adPositions", AdPosition.values());
    put("dealTypes", DealType.values());
    put("deviceTargeting", DeviceTargeting.values());
    put("adFormats", AdFormat.values());
    put("audienceTypes", AudienceType.values());
    put("audienceStatuses", AudienceStatus.values());
    put("dealStatuses", DealStatus.values());
    put("roles", Role.values());
  }

  private static <E extends Enum<E> & Labeled> void put(String key, E[] values) {
    LOOKUPS.put(
        key,
        Arrays.stream(values)
            .map(v -> new LookupOption(v.name(), v.getLabel()))
            .collect(Collectors.toList()));
  }

  @GetMapping
  public Map<String, List<LookupOption>> all() {
    return LOOKUPS;
  }
}
