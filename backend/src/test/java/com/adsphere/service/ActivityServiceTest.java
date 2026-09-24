package com.adsphere.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.adsphere.domain.ActivityAction;
import com.adsphere.domain.ActivityEvent;
import com.adsphere.domain.EntityType;
import com.adsphere.repository.ActivityEventRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

// Activity Audit Log — ActivityService unit tests (AC-A08, actor resolution, null-actor guard)
@RunWith(MockitoJUnitRunner.class)
public class ActivityServiceTest {

  @Mock private ActivityEventRepository events;
  private ActivityService service;

  @Before
  public void setUp() {
    service = new ActivityService(events);
    SecurityContextHolder.clearContext();
  }

  @After
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  // AC-A08: record() resolves actor from SecurityContextHolder
  @Test
  public void recordResolvesActorFromSecurityContext() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("admin", null));
    when(events.save(any(ActivityEvent.class))).thenAnswer(inv -> inv.getArgument(0));

    service.record(
        ActivityAction.CREATED,
        EntityType.CAMPAIGN,
        1L,
        "Test Campaign",
        "Campaign 'Test Campaign' created");

    ArgumentCaptor<ActivityEvent> captor = ArgumentCaptor.forClass(ActivityEvent.class);
    verify(events).save(captor.capture());
    ActivityEvent saved = captor.getValue();
    assertEquals("admin", saved.getActor());
    assertEquals(ActivityAction.CREATED, saved.getAction());
    assertEquals(EntityType.CAMPAIGN, saved.getEntityType());
    assertEquals(Long.valueOf(1L), saved.getEntityId());
    assertEquals("Test Campaign", saved.getEntityName());
    assertEquals("Campaign 'Test Campaign' created", saved.getSummary());
  }

  // AC-A08: record() throws when no authentication context
  @Test(expected = IllegalStateException.class)
  public void recordThrowsWhenNoAuthentication() {
    service.record(
        ActivityAction.CREATED, EntityType.CAMPAIGN, 1L, "Test", "summary");
  }

  // AC-A08: record() throws when explicit actor is blank
  @Test(expected = IllegalStateException.class)
  public void recordThrowsWhenBlankActor() {
    service.record(
        ActivityAction.CREATED, EntityType.CAMPAIGN, 1L, "Test", "summary", "");
  }

  // AC-A08: record() throws when explicit actor is null
  @Test(expected = IllegalStateException.class)
  public void recordThrowsWhenNullActor() {
    service.record(
        ActivityAction.CREATED, EntityType.CAMPAIGN, 1L, "Test", "summary", null);
  }

  // AC-A06: Explicit actor overload for DealService.placeBid()
  @Test
  public void recordWithExplicitActorPersistsCorrectly() {
    when(events.save(any(ActivityEvent.class))).thenAnswer(inv -> inv.getArgument(0));

    service.record(
        ActivityAction.BID_PLACED,
        EntityType.DEAL,
        5L,
        "Premium Deal",
        "Bid of 5000 placed on deal 'Premium Deal' by bidder",
        "bidder");

    ArgumentCaptor<ActivityEvent> captor = ArgumentCaptor.forClass(ActivityEvent.class);
    verify(events).save(captor.capture());
    ActivityEvent saved = captor.getValue();
    assertEquals("bidder", saved.getActor());
    assertEquals(ActivityAction.BID_PLACED, saved.getAction());
    assertEquals(EntityType.DEAL, saved.getEntityType());
    assertEquals(Long.valueOf(5L), saved.getEntityId());
  }

  // Verify all fields are persisted (STATUS_CHANGED / AUDIENCE path)
  @Test
  public void recordPersistsAllFields() {
    when(events.save(any(ActivityEvent.class))).thenAnswer(inv -> inv.getArgument(0));

    service.record(
        ActivityAction.STATUS_CHANGED,
        EntityType.AUDIENCE,
        42L,
        "Tech Enthusiasts",
        "Audience 'Tech Enthusiasts' status changed to INACTIVE",
        "manager");

    ArgumentCaptor<ActivityEvent> captor = ArgumentCaptor.forClass(ActivityEvent.class);
    verify(events).save(captor.capture());
    ActivityEvent saved = captor.getValue();
    assertEquals("manager", saved.getActor());
    assertEquals(ActivityAction.STATUS_CHANGED, saved.getAction());
    assertEquals(EntityType.AUDIENCE, saved.getEntityType());
    assertEquals(Long.valueOf(42L), saved.getEntityId());
    assertEquals("Tech Enthusiasts", saved.getEntityName());
    assertEquals(
        "Audience 'Tech Enthusiasts' status changed to INACTIVE", saved.getSummary());
  }
}
