package com.adsphere.web;

import com.adsphere.domain.Action;
import com.adsphere.domain.EntityType;
import com.adsphere.dto.ActivityEventResponse;
import com.adsphere.dto.PageResponse;
import com.adsphere.service.ActivityService;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {

  private final ActivityService service;

  public ActivityController(ActivityService service) {
    this.service = service;
  }

  @GetMapping
  public PageResponse<ActivityEventResponse> list(
      @RequestParam(required = false) EntityType entityType,
      @RequestParam(required = false) Action action,
      @RequestParam(required = false) String actorId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @PageableDefault(size = 10, sort = "timestamp", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return service.search(entityType, action, actorId, from, to, pageable);
  }
}
