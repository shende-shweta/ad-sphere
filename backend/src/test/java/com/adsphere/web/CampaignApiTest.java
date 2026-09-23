package com.adsphere.web;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adsphere.ApiTestSupport;
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
public class CampaignApiTest {

  @Autowired private MockMvc mvc;

  private String admin;

  @Before
  public void login() throws Exception {
    admin = ApiTestSupport.admin(mvc);
  }

  @Test
  public void requiresAuthentication() throws Exception {
    mvc.perform(get("/api/campaigns")).andExpect(status().isUnauthorized());
  }

  @Test
  public void rejectsTamperedToken() throws Exception {
    mvc.perform(get("/api/campaigns").header("Authorization", admin + "x"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  public void rejectsWrongPassword() throws Exception {
    mvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"nope\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  public void listsCampaignsWithPagingAndFilters() throws Exception {
    mvc.perform(get("/api/campaigns").param("size", "5").header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(5)))
        .andExpect(jsonPath("$.content[0].code", is("CMP001")));

    mvc.perform(get("/api/campaigns").param("status", "PAUSED").header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].status", everyItem(is("PAUSED"))));

    mvc.perform(get("/api/campaigns").param("name", "holiday").header("Authorization", admin))
        .andExpect(jsonPath("$.totalElements", is(1)))
        .andExpect(jsonPath("$.content[0].name", is("Holiday Campaign")));

    // Only campaigns overlapping June 2025.
    mvc.perform(
            get("/api/campaigns")
                .param("from", "2025-06-01")
                .param("to", "2025-06-30")
                .header("Authorization", admin))
        .andExpect(jsonPath("$.totalElements", is(1)))
        .andExpect(jsonPath("$.content[0].name", is("Product Launch")));
  }

  @Test
  public void validatesCampaign() throws Exception {
    String body =
        "{\"name\":\"\",\"objective\":\"TRAFFIC\",\"status\":\"DRAFT\","
            + "\"startDate\":\"2025-05-10\",\"endDate\":\"2025-05-01\",\"budget\":10}";
    mvc.perform(
            post("/api/campaigns")
                .header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors.name").exists())
        .andExpect(jsonPath("$.fieldErrors.budget").exists())
        .andExpect(jsonPath("$.fieldErrors.endDate", startsWith("End date")));
  }

  @Test
  public void createsCampaignWithExistingAndNewPlacement() throws Exception {
    String body =
        "{\"name\":\"Autumn Push\",\"objective\":\"REACH\",\"status\":\"DRAFT\","
            + "\"startDate\":\"2025-09-01\",\"endDate\":\"2025-09-30\",\"budget\":1500.50,"
            + "\"placementIds\":[1],"
            + "\"newPlacement\":{\"name\":\"Autumn Header\",\"country\":\"IN\",\"audienceId\":1,"
            + "\"traffic\":\"WEBSITE\",\"adPosition\":\"HEADER\",\"frequencyCap\":\"2/day\"}}";
    mvc.perform(
            post("/api/campaigns")
                .header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.code", startsWith("CMP")))
        .andExpect(jsonPath("$.placements", hasSize(2)))
        .andExpect(jsonPath("$.placements[1].name", is("Autumn Header")));
  }

  @Test
  public void rejectsDuplicateNameAndInvalidNestedPlacement() throws Exception {
    String duplicate =
        "{\"name\":\"summer sale 2025\",\"objective\":\"REACH\",\"status\":\"DRAFT\","
            + "\"startDate\":\"2025-09-01\",\"endDate\":\"2025-09-30\",\"budget\":1500}";
    mvc.perform(
            post("/api/campaigns")
                .header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(duplicate))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.fieldErrors.name").exists());

    String badPlacement =
        "{\"name\":\"Nested\",\"objective\":\"REACH\",\"status\":\"DRAFT\","
            + "\"startDate\":\"2025-09-01\",\"endDate\":\"2025-09-30\",\"budget\":1500,"
            + "\"newPlacement\":{\"name\":\"x\",\"frequencyCap\":\"lots\"}}";
    mvc.perform(
            post("/api/campaigns")
                .header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(badPlacement))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors['newPlacement.country']").exists())
        .andExpect(jsonPath("$.fieldErrors['newPlacement.frequencyCap']").exists());
  }

  @Test
  public void viewerIsReadOnly() throws Exception {
    String viewer = ApiTestSupport.viewer(mvc);
    mvc.perform(get("/api/campaigns").header("Authorization", viewer)).andExpect(status().isOk());
    mvc.perform(
            post("/api/campaigns")
                .header("Authorization", viewer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isForbidden());
    mvc.perform(
            post("/api/deals/1/bids")
                .header("Authorization", viewer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":100,\"campaignId\":1}"))
        .andExpect(status().isForbidden());
  }
}
