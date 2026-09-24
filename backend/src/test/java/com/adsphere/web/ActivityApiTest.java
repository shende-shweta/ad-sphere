package com.adsphere.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adsphere.ApiTestSupport;
import com.fasterxml.jackson.databind.JsonNode;
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

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ActivityApiTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Autowired private MockMvc mvc;

  private String admin;

  @Before
  public void login() throws Exception {
    admin = ApiTestSupport.admin(mvc);
  }

  // ---- AC-08 / AC-13: Authentication & access ----

  @Test
  public void requiresAuthentication() throws Exception {
    mvc.perform(get("/api/activity")).andExpect(status().isUnauthorized());
  }

  @Test
  public void allAuthenticatedRolesCanRead() throws Exception {
    String viewer = ApiTestSupport.viewer(mvc);
    mvc.perform(get("/api/activity").header("Authorization", viewer))
        .andExpect(status().isOk());
  }

  // ---- AC-06 / AC-13: Immutability — write verbs rejected ----

  @Test
  public void postRejectedWith403() throws Exception {
    mvc.perform(
            post("/api/activity")
                .header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isForbidden());
  }

  @Test
  public void putRejectedWith403() throws Exception {
    mvc.perform(
            put("/api/activity")
                .header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isForbidden());
  }

  @Test
  public void patchRejectedWith403() throws Exception {
    mvc.perform(
            patch("/api/activity")
                .header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isForbidden());
  }

  @Test
  public void deleteRejectedWith403() throws Exception {
    mvc.perform(delete("/api/activity").header("Authorization", admin))
        .andExpect(status().isForbidden());
  }

  // ---- AC-09: PageResponse envelope & defaults ----

  @Test
  public void returnsPageResponseEnvelope() throws Exception {
    mvc.perform(get("/api/activity").header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.page").isNumber())
        .andExpect(jsonPath("$.size").isNumber())
        .andExpect(jsonPath("$.totalElements").isNumber())
        .andExpect(jsonPath("$.totalPages").isNumber());
  }

  @Test
  public void defaultPageSizeIsTen() throws Exception {
    mvc.perform(get("/api/activity").header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size", is(10)));
  }

  // ---- AC-01: Campaign event capture ----

  @Test
  public void campaignCreateRecordsCreatedEvent() throws Exception {
    createCampaign("ActLog Create Test");

    mvc.perform(
            get("/api/activity")
                .param("entityType", "CAMPAIGN")
                .param("action", "CREATED")
                .header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[?(@.entityName=='ActLog Create Test')].action",
            hasItem("CREATED")))
        .andExpect(jsonPath("$.content[?(@.entityName=='ActLog Create Test')].entityType",
            hasItem("CAMPAIGN")))
        .andExpect(jsonPath("$.content[?(@.entityName=='ActLog Create Test')].actor",
            hasItem("admin")))
        .andExpect(jsonPath("$.content[?(@.entityName=='ActLog Create Test')].summary",
            hasItem(containsString("Created campaign"))));
  }

  @Test
  public void campaignUpdateRecordsUpdatedEvent() throws Exception {
    long id = createCampaign("ActLog Update Before");

    String updateBody =
        "{\"name\":\"ActLog Update After\",\"objective\":\"REACH\",\"status\":\"DRAFT\","
            + "\"startDate\":\"2026-11-01\",\"endDate\":\"2026-11-30\",\"budget\":6000}";
    mvc.perform(
            put("/api/campaigns/" + id)
                .header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
        .andExpect(status().isOk());

    mvc.perform(
            get("/api/activity")
                .param("entityType", "CAMPAIGN")
                .param("action", "UPDATED")
                .header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[?(@.entityName=='ActLog Update After')].action",
            hasItem("UPDATED")))
        .andExpect(jsonPath("$.content[?(@.entityName=='ActLog Update After')].summary",
            hasItem(containsString("Updated campaign"))));
  }

  @Test
  public void campaignDeleteRecordsDeletedEventWithPreservedName() throws Exception {
    long id = createCampaign("ActLog Delete Target");

    mvc.perform(delete("/api/campaigns/" + id).header("Authorization", admin))
        .andExpect(status().isNoContent());

    mvc.perform(
            get("/api/activity")
                .param("entityType", "CAMPAIGN")
                .param("action", "DELETED")
                .header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[?(@.entityName=='ActLog Delete Target')].action",
            hasItem("DELETED")))
        .andExpect(jsonPath("$.content[?(@.entityName=='ActLog Delete Target')].summary",
            hasItem(containsString("Deleted campaign 'ActLog Delete Target'"))));
  }

  // ---- AC-05: Transactional consistency (event fields present) ----

  @Test
  public void activityEventContainsAllRequiredFields() throws Exception {
    createCampaign("ActLog Fields Check");

    mvc.perform(
            get("/api/activity")
                .param("entityType", "CAMPAIGN")
                .param("action", "CREATED")
                .header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").isNumber())
        .andExpect(jsonPath("$.content[0].action").isString())
        .andExpect(jsonPath("$.content[0].entityType").isString())
        .andExpect(jsonPath("$.content[0].entityId").isNumber())
        .andExpect(jsonPath("$.content[0].entityName").isString())
        .andExpect(jsonPath("$.content[0].actor").isString())
        .andExpect(jsonPath("$.content[0].summary").isString())
        .andExpect(jsonPath("$.content[0].timestamp").isString());
  }

  // ---- AC-19: Actor resolution from SecurityContext ----

  @Test
  public void actorResolvedFromSecurityContext() throws Exception {
    createCampaign("ActLog Actor Check");

    mvc.perform(
            get("/api/activity")
                .param("entityType", "CAMPAIGN")
                .header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.content[?(@.entityName=='ActLog Actor Check')].actor",
                hasItem("admin")));
  }

  // ---- AC-10: Filtering ----

  @Test
  public void filterByEntityType() throws Exception {
    createCampaign("ActLog Filter Entity");

    mvc.perform(
            get("/api/activity")
                .param("entityType", "CAMPAIGN")
                .header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].entityType", everyItem(is("CAMPAIGN"))));
  }

  @Test
  public void filterByAction() throws Exception {
    createCampaign("ActLog Filter Action");

    mvc.perform(
            get("/api/activity")
                .param("action", "CREATED")
                .header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].action", everyItem(is("CREATED"))));
  }

  @Test
  public void filterByActorId() throws Exception {
    createCampaign("ActLog Filter Actor");

    mvc.perform(
            get("/api/activity")
                .param("actorId", "admin")
                .header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].actor", everyItem(containsString("admin"))));
  }

  @Test
  public void filterByDateRange() throws Exception {
    createCampaign("ActLog Filter Date");

    mvc.perform(
            get("/api/activity")
                .param("from", "2026-09-01")
                .param("to", "2026-12-31")
                .header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isNotEmpty());
  }

  @Test
  public void combinedFiltersUseAndSemantics() throws Exception {
    createCampaign("ActLog Combined Filter");

    mvc.perform(
            get("/api/activity")
                .param("entityType", "CAMPAIGN")
                .param("action", "CREATED")
                .param("actorId", "admin")
                .header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].entityType", everyItem(is("CAMPAIGN"))))
        .andExpect(jsonPath("$.content[*].action", everyItem(is("CREATED"))))
        .andExpect(jsonPath("$.content[*].actor", everyItem(containsString("admin"))));
  }

  @Test
  public void noFiltersReturnsAllEvents() throws Exception {
    createCampaign("ActLog No Filter");

    mvc.perform(get("/api/activity").header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)));
  }

  @Test
  public void clearedFiltersReturnAll() throws Exception {
    createCampaign("ActLog Clear Filter");

    mvc.perform(
            get("/api/activity")
                .param("entityType", "DEAL")
                .header("Authorization", admin))
        .andExpect(status().isOk());

    mvc.perform(get("/api/activity").header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)));
  }

  // ---- AC-14: Input validation ----

  @Test
  public void invalidEntityTypeReturns400() throws Exception {
    mvc.perform(
            get("/api/activity")
                .param("entityType", "INVALID_TYPE")
                .header("Authorization", admin))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void invalidActionReturns400() throws Exception {
    mvc.perform(
            get("/api/activity")
                .param("action", "INVALID_ACTION")
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

  @Test
  public void emptyFilterParamsReturnAll() throws Exception {
    createCampaign("ActLog Empty Params");

    mvc.perform(
            get("/api/activity")
                .param("entityType", "")
                .param("action", "")
                .header("Authorization", admin))
        .andExpect(status().isOk());
  }

  // ---- AC-17: Pagination ----

  @Test
  public void paginationWorksCorrectly() throws Exception {
    for (int i = 0; i < 3; i++) {
      createCampaign("ActLog Page " + i);
    }

    mvc.perform(
            get("/api/activity")
                .param("size", "2")
                .param("page", "0")
                .header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(2)))
        .andExpect(jsonPath("$.size", is(2)))
        .andExpect(jsonPath("$.page", is(0)));
  }

  @Test
  public void sortedNewestFirst() throws Exception {
    createCampaign("ActLog Sort A");
    createCampaign("ActLog Sort B");

    mvc.perform(
            get("/api/activity")
                .param("entityType", "CAMPAIGN")
                .header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].entityName", is("ActLog Sort B")));
  }

  // ---- Helpers ----

  private long createCampaign(String name) throws Exception {
    String body =
        "{\"name\":\"" + name + "\",\"objective\":\"REACH\",\"status\":\"DRAFT\","
            + "\"startDate\":\"2026-11-01\",\"endDate\":\"2026-11-30\",\"budget\":5000}";
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
}
