package com.adsphere.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AudienceApiTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Autowired private MockMvc mvc;

  private String admin;

  @Before
  public void login() throws Exception {
    admin = ApiTestSupport.admin(mvc);
  }

  // --- POST (Create) ---

  @Test
  public void createAsManagerReturns201WithLocationAndDefaults() throws Exception {
    String manager = ApiTestSupport.bearer(mvc, "manager", "Manager@123");
    String body =
        "{\"name\":\"Manager Created Audience\",\"type\":\"INTEREST\","
            + "\"estimatedSize\":5000000,\"description\":\"Test description\"}";
    mvc.perform(
            post("/api/audiences")
                .header("Authorization", manager)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", containsString("/api/audiences/")))
        .andExpect(jsonPath("$.status", is("ACTIVE")))
        .andExpect(jsonPath("$.placementCount", is(0)))
        .andExpect(jsonPath("$.version", is(0)))
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.updatedAt").exists());
  }

  @Test
  public void createAsViewerReturns403() throws Exception {
    String viewer = ApiTestSupport.viewer(mvc);
    mvc.perform(
            post("/api/audiences")
                .header("Authorization", viewer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\":\"Viewer Test\",\"type\":\"DEMOGRAPHIC\",\"estimatedSize\":1000}"))
        .andExpect(status().isForbidden());
  }

  @Test
  public void createWithInvalidFieldsReturns400WithAllFieldErrors() throws Exception {
    String longDesc = "x".repeat(501);
    String body =
        "{\"name\":\"\",\"estimatedSize\":999,\"description\":\"" + longDesc + "\"}";
    mvc.perform(
            post("/api/audiences")
                .header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors.name").exists())
        .andExpect(jsonPath("$.fieldErrors.type").exists())
        .andExpect(jsonPath("$.fieldErrors.estimatedSize").exists())
        .andExpect(jsonPath("$.fieldErrors.description").exists());
  }

  @Test
  public void createWithDuplicateNameCaseInsensitiveReturns422() throws Exception {
    String body =
        "{\"name\":\"tech enthusiasts\",\"type\":\"INTEREST\",\"estimatedSize\":1000000}";
    mvc.perform(
            post("/api/audiences")
                .header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            jsonPath(
                "$.fieldErrors.name", is("An audience with this name already exists")));
  }

  @Test
  public void createAsManagerWithInactiveStatusReturns403() throws Exception {
    String manager = ApiTestSupport.bearer(mvc, "manager", "Manager@123");
    mvc.perform(
            post("/api/audiences")
                .header("Authorization", manager)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\":\"Manager Status Guard\",\"type\":\"DEMOGRAPHIC\","
                        + "\"estimatedSize\":5000,\"status\":\"INACTIVE\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  public void createAsAdminWithInactiveStatusReturns201() throws Exception {
    mvc.perform(
            post("/api/audiences")
                .header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\":\"Admin Inactive Audience\",\"type\":\"LOCATION\","
                        + "\"estimatedSize\":10000,\"status\":\"INACTIVE\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status", is("INACTIVE")));
  }

  // --- GET (single) ---

  @Test
  public void getByIdReturns200WithAllFields() throws Exception {
    mvc.perform(get("/api/audiences/1").header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.name").isString())
        .andExpect(jsonPath("$.type").isString())
        .andExpect(jsonPath("$.status").isString())
        .andExpect(jsonPath("$.estimatedSize").isNumber())
        .andExpect(jsonPath("$.version").isNumber())
        .andExpect(jsonPath("$.placementCount").isNumber())
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.updatedAt").exists());
  }

  @Test
  public void getByUnknownIdReturns404() throws Exception {
    mvc.perform(get("/api/audiences/99999").header("Authorization", admin))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message", containsString("not found")));
  }

  // --- PUT (Update) ---

  @Test
  public void updateKeepingSameNameSucceeds() throws Exception {
    long id = createAudience("Same Name Update", "BEHAVIORAL", 50000);
    mvc.perform(
            put("/api/audiences/" + id)
                .header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\":\"Same Name Update\",\"type\":\"BEHAVIORAL\","
                        + "\"estimatedSize\":75000,\"description\":\"Updated\",\"version\":0}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.estimatedSize", is(75000)))
        .andExpect(jsonPath("$.version", is(1)));
  }

  @Test
  public void updateRenamingToDuplicateReturns422() throws Exception {
    long id = createAudience("Rename Dup Source", "INCOME", 100000);
    mvc.perform(
            put("/api/audiences/" + id)
                .header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\":\"tech enthusiasts\",\"type\":\"INCOME\","
                        + "\"estimatedSize\":100000,\"version\":0}"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.fieldErrors.name").exists());
  }

  @Test
  public void updateWithStaleVersionReturns409() throws Exception {
    long id = createAudience("Version Conflict", "DEVICE", 200000);
    mvc.perform(
            put("/api/audiences/" + id)
                .header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\":\"Version Conflict\",\"type\":\"DEVICE\","
                        + "\"estimatedSize\":250000,\"version\":0}"))
        .andExpect(status().isOk());
    mvc.perform(
            put("/api/audiences/" + id)
                .header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\":\"Version Conflict\",\"type\":\"DEVICE\","
                        + "\"estimatedSize\":300000,\"version\":0}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message", containsString("changed by someone else")));
  }

  // --- GET (list with extended fields) ---

  @Test
  public void listReturnsExtendedFieldsIncludingPlacementCount() throws Exception {
    mvc.perform(get("/api/audiences").param("size", "20").header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].version").isNumber())
        .andExpect(jsonPath("$.content[0].placementCount").isNumber())
        .andExpect(jsonPath("$.content[0].createdAt").exists())
        .andExpect(jsonPath("$.content[0].updatedAt").exists());
  }

  private long createAudience(String name, String type, long estimatedSize) throws Exception {
    String body =
        "{\"name\":\"" + name + "\",\"type\":\"" + type + "\","
            + "\"estimatedSize\":" + estimatedSize + "}";
    String result =
        mvc.perform(
                post("/api/audiences")
                    .header("Authorization", admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return MAPPER.readTree(result).get("id").asLong();
  }
}
