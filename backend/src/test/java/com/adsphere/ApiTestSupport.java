package com.adsphere;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Logs in through the real endpoint so tests exercise JWT issuance and validation. */
public final class ApiTestSupport {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private ApiTestSupport() {}

  public static String bearer(MockMvc mvc, String username, String password) throws Exception {
    String body =
        mvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    JsonNode json = MAPPER.readTree(body);
    return "Bearer " + json.get("token").asText();
  }

  public static String admin(MockMvc mvc) throws Exception {
    return bearer(mvc, "admin", "Admin@123");
  }

  public static String viewer(MockMvc mvc) throws Exception {
    return bearer(mvc, "viewer", "Viewer@123");
  }
}
