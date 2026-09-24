package com.adsphere.web;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adsphere.ApiTestSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

// Activity Audit Log — API integration tests (AC-A01 through AC-D02)
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ActivityApiTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Autowired private MockMvc mvc;

  private String admin;
  private String viewer;

  @Before
  public void login() throws Exception {
    admin = ApiTestSupport.admin(mvc);
    viewer = ApiTestSupport.viewer(mvc);
  }

  private long createCampaign(String name) throws Exception {
    String body =
        "{\"name\":\"" + name + "\",\"objective\":\"REACH\",\"status\":\"DRAFT\","
            + "\"startDate\":\"2026-09-01\",\"endDate\":\"2026-09-30\",\"budget\":1000}";
    String response =
        mvc.perform(
                post("/api/campaigns")
                    .header("Authorization", admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return MAPPER.readTree(response).get("id").asLong();
  }

  // AC-A01: Campaign create records an activity event with correct fields
  @Test
  public void campaignCreateRecordsActivityEvent() throws Exception {
    createCampaign("Activity Test Alpha");
    mvc.perform(
            get("/api/activity")
                .param("entityType", "CAMPAIGN")
                .param("action", "CREATED")
                .header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath(
                "$.content[?(@.entityName == 'Activity Test Alpha')].action",
                hasItem("CREATED")))
        .andExpect(
            jsonPath(
                "$.content[?(@.entityName == 'Activity Test Alpha')].entityType",
                hasItem("CAMPAIGN")))
        .andExpect(
            jsonPath(
                "$.content[?(@.entityName == 'Activity Test Alpha')].actor",
                hasItem("admin")));
  }

  // AC-B01: Default pagination (page=0, size=10, sorted by timestamp desc)
  @Test
  public void defaultPagination() throws Exception {
    mvc.perform(get("/api/activity").header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page", is(0)))
        .andExpect(jsonPath("$.size", is(10)))
        .andExpect(jsonPath("$.totalElements").isNumber())
        .andExpect(jsonPath("$.totalPages").isNumber())
        .andExpect(jsonPath("$.content").isArray());
  }

  // AC-B01: Response contains all required DTO fields
  @Test
  public void responseContainsAllRequiredFields() throws Exception {
    createCampaign("Fields Validation Camp");
    mvc.perform(
            get("/api/activity")
                .param("entityType", "CAMPAIGN")
                .header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").isNumber())
        .andExpect(jsonPath("$.content[0].actor").isString())
        .andExpect(jsonPath("$.content[0].action").isString())
        .andExpect(jsonPath("$.content[0].entityType").isString())
        .andExpect(jsonPath("$.content[0].entityId").isNumber())
        .andExpect(jsonPath("$.content[0].entityName").isString())
        .andExpect(jsonPath("$.content[0].summary").isString())
        .andExpect(jsonPath("$.content[0].timestamp").isString());
  }

  // AC-B02: Filter by entity type returns only matching entries
  @Test
  public void filterByEntityType() throws Exception {
    createCampaign("ET Filter Camp");
    mvc.perform(
            get("/api/activity")
                .param("entityType", "CAMPAIGN")
                .header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].entityType", everyItem(is("CAMPAIGN"))));
  }

  // AC-B03: Filter by action type returns only matching entries
  @Test
  public void filterByAction() throws Exception {
    createCampaign("Action Filter Camp");
    mvc.perform(
            get("/api/activity")
                .param("action", "CREATED")
                .header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].action", everyItem(is("CREATED"))));
  }

  // AC-B04: Filter by actor returns only matching entries; non-existent actor returns empty
  @Test
  public void filterByActor() throws Exception {
    createCampaign("Actor Filter Camp");
    mvc.perform(
            get("/api/activity")
                .param("actorId", "admin")
                .header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].actor", everyItem(is("admin"))));

    mvc.perform(
            get("/api/activity")
                .param("actorId", "nonexistent_user")
                .header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements", is(0)));
  }

  // AC-B05: Filter by date range
  @Test
  public void filterByDateRange() throws Exception {
    createCampaign("Date Range Camp");
    mvc.perform(
            get("/api/activity")
                .param("from", "2026-09-24")
                .param("to", "2026-09-24")
                .header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());

    mvc.perform(
            get("/api/activity")
                .param("from", "2020-01-01")
                .param("to", "2020-01-01")
                .header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements", is(0)));
  }

  // AC-B06: Combined filters use AND logic
  @Test
  public void combinedFiltersUseAndLogic() throws Exception {
    createCampaign("Combined Filter Camp");
    mvc.perform(
            get("/api/activity")
                .param("entityType", "CAMPAIGN")
                .param("action", "CREATED")
                .param("actorId", "admin")
                .header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].entityType", everyItem(is("CAMPAIGN"))))
        .andExpect(jsonPath("$.content[*].action", everyItem(is("CREATED"))))
        .andExpect(jsonPath("$.content[*].actor", everyItem(is("admin"))));
  }

  // AC-B07: Invalid enum values return HTTP 400
  @Test
  public void invalidEntityTypeReturns400() throws Exception {
    mvc.perform(
            get("/api/activity")
                .param("entityType", "INVALID")
                .header("Authorization", admin))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void invalidActionReturns400() throws Exception {
    mvc.perform(
            get("/api/activity")
                .param("action", "INVALID")
                .header("Authorization", admin))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void invalidDateFormatReturns400() throws Exception {
    mvc.perform(
            get("/api/activity")
                .param("from", "not-a-date")
                .header("Authorization", admin))
        .andExpect(status().isBadRequest());
  }

  // AC-B08: Unauthenticated access returns 401
  @Test
  public void unauthenticatedReturns401() throws Exception {
    mvc.perform(get("/api/activity")).andExpect(status().isUnauthorized());
  }

  // AC-B09: All authenticated roles (ADMIN, VIEWER) can read
  @Test
  public void allRolesCanRead() throws Exception {
    mvc.perform(get("/api/activity").header("Authorization", admin))
        .andExpect(status().isOk());
    mvc.perform(get("/api/activity").header("Authorization", viewer))
        .andExpect(status().isOk());
  }

  // AC-B10: POST rejected (read-only endpoint)
  @Test
  public void postRejected() throws Exception {
    int status =
        mvc.perform(
                post("/api/activity")
                    .header("Authorization", admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
            .andReturn()
            .getResponse()
            .getStatus();
    assertTrue("POST should return 403 or 405, got " + status, status == 403 || status == 405);
  }

  // AC-B10: PUT rejected
  @Test
  public void putRejected() throws Exception {
    int status =
        mvc.perform(
                put("/api/activity")
                    .header("Authorization", admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
            .andReturn()
            .getResponse()
            .getStatus();
    assertTrue("PUT should return 403 or 405, got " + status, status == 403 || status == 405);
  }

  // AC-B10: DELETE rejected
  @Test
  public void deleteRejected() throws Exception {
    int status =
        mvc.perform(delete("/api/activity").header("Authorization", admin))
            .andReturn()
            .getResponse()
            .getStatus();
    assertTrue(
        "DELETE should return 403 or 405, got " + status, status == 403 || status == 405);
  }

  // AC-A07: Failed operation (duplicate name) produces no audit entry
  @Test
  public void failedCreateProducesNoAuditEntry() throws Exception {
    createCampaign("Unique For Dup Test");

    String beforeJson =
        mvc.perform(
                get("/api/activity")
                    .param("entityType", "CAMPAIGN")
                    .header("Authorization", admin))
            .andReturn()
            .getResponse()
            .getContentAsString();
    int countBefore = MAPPER.readTree(beforeJson).get("totalElements").asInt();

    mvc.perform(
            post("/api/campaigns")
                .header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\":\"unique for dup test\",\"objective\":\"REACH\","
                        + "\"status\":\"DRAFT\",\"startDate\":\"2026-09-01\","
                        + "\"endDate\":\"2026-09-30\",\"budget\":1000}"))
        .andExpect(status().isUnprocessableEntity());

    String afterJson =
        mvc.perform(
                get("/api/activity")
                    .param("entityType", "CAMPAIGN")
                    .header("Authorization", admin))
            .andReturn()
            .getResponse()
            .getContentAsString();
    int countAfter = MAPPER.readTree(afterJson).get("totalElements").asInt();

    assertEquals(
        "Failed create should not produce an activity event", countBefore, countAfter);
  }

  // AC-D02: Existing campaign API contract is unchanged
  @Test
  public void existingCampaignEndpointUnchanged() throws Exception {
    mvc.perform(get("/api/campaigns").header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].code").exists())
        .andExpect(jsonPath("$.content[0].name").exists())
        .andExpect(jsonPath("$.content[0].status").exists());
  }
}
