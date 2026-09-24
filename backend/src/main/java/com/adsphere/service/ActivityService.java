package com.adsphere.service;

import com.adsphere.domain.ActivityAction;
import com.adsphere.domain.ActivityEvent;
import com.adsphere.domain.EntityType;
import com.adsphere.dto.ActivityEventResponse;
import com.adsphere.dto.PageResponse;
import com.adsphere.repository.ActivityEventRepository;
import com.adsphere.repository.Specs;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class ActivityService {

  private final ActivityEventRepository events;

  public ActivityService(ActivityEventRepository events) {
    this.events = events;
  }

  public void record(
      ActivityAction action,
      EntityType entityType,
      Long entityId,
      String entityName,
      String summary) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !StringUtils.hasText(auth.getName())) {
      throw new IllegalStateException("Cannot record activity: no authenticated user");
    }
    record(action, entityType, entityId, entityName, summary, auth.getName());
  }

  public void record(
      ActivityAction action,
      EntityType entityType,
      Long entityId,
      String entityName,
      String summary,
      String actor) {
    if (!StringUtils.hasText(actor)) {
      throw new IllegalStateException("Cannot record activity: actor is blank");
    }
    events.save(new ActivityEvent(actor, action, entityType, entityId, entityName, summary));
  }

  @Transactional(readOnly = true)
  public PageResponse<ActivityEventResponse> search(
      EntityType entityType,
      ActivityAction action,
      String actorId,
      LocalDate from,
      LocalDate to,
      Pageable pageable) {
    Specification<ActivityEvent> spec =
        Specification.where(Specs.<ActivityEvent>equalTo("entityType", entityType))
            .and(Specs.equalTo("action", action))
            .and(
                StringUtils.hasText(actorId)
                    ? Specs.equalTo("actor", actorId)
                    : Specs.all());
    if (from != null) {
      Instant fromInstant = from.atStartOfDay(ZoneOffset.UTC).toInstant();
      spec =
          spec.and(
              (root, q, cb) -> cb.greaterThanOrEqualTo(root.get("timestamp"), fromInstant));
    }
    if (to != null) {
      Instant toInstant = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
      spec = spec.and((root, q, cb) -> cb.lessThan(root.get("timestamp"), toInstant));
    }
    return PageResponse.of(events.findAll(spec, pageable), ActivityEventResponse::from);
  }
}
