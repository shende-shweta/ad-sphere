package com.adsphere.service;

import com.adsphere.domain.Action;
import com.adsphere.domain.ActivityEvent;
import com.adsphere.domain.EntityType;
import com.adsphere.dto.ActivityEventResponse;
import com.adsphere.dto.PageResponse;
import com.adsphere.repository.ActivityEventRepository;
import com.adsphere.repository.Specs;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ActivityService {

  private final ActivityEventRepository events;

  public ActivityService(ActivityEventRepository events) {
    this.events = events;
  }

  public void record(
      Action action, EntityType entityType, Long entityId, String entityName, String summary) {
    String actor = SecurityContextHolder.getContext().getAuthentication().getName();
    events.save(new ActivityEvent(action, entityType, entityId, entityName, actor, summary));
  }

  @Transactional(readOnly = true)
  public PageResponse<ActivityEventResponse> search(
      EntityType entityType,
      Action action,
      String actorId,
      LocalDate from,
      LocalDate to,
      Pageable pageable) {
    Specification<ActivityEvent> spec =
        Specification.where(Specs.<ActivityEvent>equalTo("entityType", entityType))
            .and(Specs.equalTo("action", action))
            .and(Specs.containsText(actorId, "actor"));
    if (from != null) {
      spec =
          spec.and(
              (root, q, cb) ->
                  cb.greaterThanOrEqualTo(
                      root.get("timestamp"), from.atStartOfDay().toInstant(ZoneOffset.UTC)));
    }
    if (to != null) {
      spec =
          spec.and(
              (root, q, cb) ->
                  cb.lessThan(
                      root.get("timestamp"),
                      to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)));
    }
    return PageResponse.of(events.findAll(spec, pageable), ActivityEventResponse::from);
  }
}
