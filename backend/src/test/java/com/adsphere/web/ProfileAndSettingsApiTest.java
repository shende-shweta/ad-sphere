package com.adsphere.web;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adsphere.ApiTestSupport;
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
public class ProfileAndSettingsApiTest {

  @Autowired private MockMvc mvc;

  @Test
  public void onlyAdminsUpdateSettings() throws Exception {
    String admin = ApiTestSupport.admin(mvc);
    String viewer = ApiTestSupport.viewer(mvc);
    String body =
        "{\"platformName\":\"AdSphere Pro\",\"timezone\":\"UTC\",\"language\":\"en-US\","
            + "\"currency\":\"EUR\",\"sessionTimeoutMinutes\":30,\"passwordMinLength\":10}";

    mvc.perform(
            put("/api/settings")
                .header("Authorization", viewer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isForbidden());

    mvc.perform(
            put("/api/settings")
                .header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.platformName", is("AdSphere Pro")));

    mvc.perform(
            put("/api/settings")
                .header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.replace("\"EUR\"", "\"euro\"")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors.currency").exists());
  }

  @Test
  public void updatesProfileAndChangesPassword() throws Exception {
    String manager = ApiTestSupport.bearer(mvc, "manager", "Manager@123");

    mvc.perform(get("/api/profile").header("Authorization", manager))
        .andExpect(jsonPath("$.role", is("MANAGER")));

    mvc.perform(
            put("/api/profile")
                .header("Authorization", manager)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"Alex M.\",\"email\":\"not-an-email\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors.email").exists());

    mvc.perform(
            put("/api/profile/password")
                .header("Authorization", manager)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"wrong\",\"newPassword\":\"Another123\"}"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.fieldErrors.currentPassword").exists());

    mvc.perform(
            put("/api/profile/password")
                .header("Authorization", manager)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"Manager@123\",\"newPassword\":\"Another123\"}"))
        .andExpect(status().isNoContent());

    ApiTestSupport.bearer(mvc, "manager", "Another123");
  }

  @Test
  public void createsSupportTicket() throws Exception {
    String viewer = ApiTestSupport.viewer(mvc);
    mvc.perform(get("/api/help/faqs").header("Authorization", viewer)).andExpect(status().isOk());
    mvc.perform(
            post("/api/help/tickets")
                .header("Authorization", viewer)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"subject\":\"Report\",\"message\":\"The export button is missing.\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists());
  }
}
