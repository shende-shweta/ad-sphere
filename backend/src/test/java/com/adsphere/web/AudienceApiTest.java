package com.adsphere.web;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

// SCRUM-108: Create & Edit Audience — MockMvc Integration Tests
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AudienceApiTest {

  @Autowired private MockMvc mvc;

  private static final String BASE = "/api/audiences";

  private String validPayload(String name) {
    return "{" +
        "\"name\":\"" + name + "\"," +
        "\"type\":\"INTEREST\"," +
        "\"description\":\"Test audience\"," +
        "\"estimatedSize\":5000000" +
        "}";
  }

  // ── POST /api/audiences ──────────────────────────────────────────

  @Test
  public void createAsManager_returns201() throws Exception {
    mvc.perform(
            post(BASE)
                .with(user("mgr").roles("MANAGER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload("Manager Created Audience")))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.name").value("Manager Created Audience"))
        .andExpect(jsonPath("$.status").value("ACTIVE"))
        .andExpect(jsonPath("$.version").value(0))
        .andExpect(jsonPath("$.placementCount").value(0));
  }

  @Test
  public void createAsAdmin_returns201WithAllFields() throws Exception {
    mvc.perform(
            post(BASE)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload("Admin Created Audience")))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", containsString("/api/audiences/")))
        .andExpect(jsonPath("$.name").value("Admin Created Audience"))
        .andExpect(jsonPath("$.type").value("INTEREST"))
        .andExpect(jsonPath("$.estimatedSize").value(5000000))
        .andExpect(jsonPath("$.status").value("ACTIVE"))
        .andExpect(jsonPath("$.version").value(0))
        .andExpect(jsonPath("$.placementCount").value(0))
        .andExpect(jsonPath("$.createdAt").isString())
        .andExpect(jsonPath("$.updatedAt").isString());
  }

  @Test
  public void createAsViewer_returns403() throws Exception {
    mvc.perform(
            post(BASE)
                .with(user("viewer").roles("VIEWER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload("Viewer Audience")))
        .andExpect(status().isForbidden());
  }

  @Test
  public void createWithInvalidFields_returns400() throws Exception {
    String body = "{" +
        "\"name\":\"\"," +
        "\"estimatedSize\":999," +
        "\"description\":\"" + "x".repeat(501) + "\"" +
        "}";
    mvc.perform(
            post(BASE)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors.name").exists())
        .andExpect(jsonPath("$.fieldErrors.type").exists())
        .andExpect(jsonPath("$.fieldErrors.estimatedSize").exists())
        .andExpect(jsonPath("$.fieldErrors.description").exists());
  }

  @Test
  public void createWithShortName_returns400() throws Exception {
    String body = "{" +
        "\"name\":\"Ab\"," +
        "\"type\":\"INTEREST\"," +
        "\"estimatedSize\":5000000" +
        "}";
    mvc.perform(
            post(BASE)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors.name").exists());
  }

  @Test
  public void createDuplicateName_returns422() throws Exception {
    mvc.perform(
            post(BASE)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload("Unique Name For Dup Test")))
        .andExpect(status().isCreated());

    mvc.perform(
            post(BASE)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload("unique name for dup test")))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            jsonPath("$.fieldErrors.name")
                .value("An audience with this name already exists"));
  }

  @Test
  public void createAsManagerWithInactiveStatus_returns403() throws Exception {
    String body = "{" +
        "\"name\":\"Manager Inactive Test\"," +
        "\"type\":\"DEMOGRAPHIC\"," +
        "\"estimatedSize\":2000000," +
        "\"status\":\"INACTIVE\"" +
        "}";
    mvc.perform(
            post(BASE)
                .with(user("mgr").roles("MANAGER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isForbidden());
  }

  @Test
  public void createAsAdminWithInactiveStatus_returns201() throws Exception {
    String body = "{" +
        "\"name\":\"Admin Inactive Audience\"," +
        "\"type\":\"DEMOGRAPHIC\"," +
        "\"estimatedSize\":2000000," +
        "\"status\":\"INACTIVE\"" +
        "}";
    mvc.perform(
            post(BASE)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("INACTIVE"));
  }

  @Test
  public void createTrimsName() throws Exception {
    String body = "{" +
        "\"name\":\"  Trimmed Name Test  \"," +
        "\"type\":\"LOCATION\"," +
        "\"estimatedSize\":5000000" +
        "}";
    mvc.perform(
            post(BASE)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Trimmed Name Test"));
  }

  // ── GET /api/audiences/{id} ──────────────────────────────────────

  @Test
  public void getById_returns200() throws Exception {
    mvc.perform(get(BASE + "/1").with(user("viewer").roles("VIEWER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
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
  public void getByUnknownId_returns404() throws Exception {
    mvc.perform(get(BASE + "/99999").with(user("viewer").roles("VIEWER")))
        .andExpect(status().isNotFound());
  }

  // ── PUT /api/audiences/{id} ──────────────────────────────────────

  @Test
  public void updateKeepingSameName_returns200() throws Exception {
    String createResult =
        mvc.perform(
                post(BASE)
                    .with(user("admin").roles("ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validPayload("Update Same Name Test")))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    String id = createResult.replaceAll(".*\"id\":(\\d+).*", "$1");
    String updateBody = "{" +
        "\"name\":\"Update Same Name Test\"," +
        "\"type\":\"LOCATION\"," +
        "\"estimatedSize\":8000000," +
        "\"version\":0" +
        "}";
    mvc.perform(
            put(BASE + "/" + id)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.type").value("LOCATION"))
        .andExpect(jsonPath("$.estimatedSize").value(8000000))
        .andExpect(jsonPath("$.version").value(1));
  }

  @Test
  public void updateWithDuplicateName_returns422() throws Exception {
    mvc.perform(
            post(BASE)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload("Dup Target Audience")))
        .andExpect(status().isCreated());

    String createResult =
        mvc.perform(
                post(BASE)
                    .with(user("admin").roles("ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validPayload("Dup Source Audience")))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    String id = createResult.replaceAll(".*\"id\":(\\d+).*", "$1");
    String updateBody = "{" +
        "\"name\":\"dup target audience\"," +
        "\"type\":\"INTEREST\"," +
        "\"estimatedSize\":5000000," +
        "\"version\":0" +
        "}";
    mvc.perform(
            put(BASE + "/" + id)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            jsonPath("$.fieldErrors.name")
                .value("An audience with this name already exists"));
  }

  @Test
  public void updateWithStaleVersion_returns409() throws Exception {
    String createResult =
        mvc.perform(
                post(BASE)
                    .with(user("admin").roles("ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validPayload("Stale Version Test")))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    String id = createResult.replaceAll(".*\"id\":(\\d+).*", "$1");
    String updateBody = "{" +
        "\"name\":\"Stale Version Test\"," +
        "\"type\":\"INTEREST\"," +
        "\"estimatedSize\":5000000," +
        "\"version\":999" +
        "}";
    mvc.perform(
            put(BASE + "/" + id)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
        .andExpect(status().isConflict());
  }

  @Test
  public void updateAsViewer_returns403() throws Exception {
    String updateBody = "{" +
        "\"name\":\"Viewer Update Test\"," +
        "\"type\":\"INTEREST\"," +
        "\"estimatedSize\":5000000," +
        "\"version\":0" +
        "}";
    mvc.perform(
            put(BASE + "/1")
                .with(user("viewer").roles("VIEWER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
        .andExpect(status().isForbidden());
  }

  @Test
  public void updateUnknownId_returns404() throws Exception {
    String updateBody = "{" +
        "\"name\":\"Unknown Update Test\"," +
        "\"type\":\"INTEREST\"," +
        "\"estimatedSize\":5000000," +
        "\"version\":0" +
        "}";
    mvc.perform(
            put(BASE + "/99999")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
        .andExpect(status().isNotFound());
  }

  // ── GET /api/audiences (list) ────────────────────────────────────

  @Test
  public void listIncludesPlacementCount() throws Exception {
    mvc.perform(get(BASE).with(user("viewer").roles("VIEWER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].placementCount").isNumber());
  }

  @Test
  public void listReturnsPageStructure() throws Exception {
    mvc.perform(get(BASE).with(user("viewer").roles("VIEWER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.page").isNumber())
        .andExpect(jsonPath("$.totalElements").isNumber())
        .andExpect(jsonPath("$.totalPages").isNumber());
  }
}
