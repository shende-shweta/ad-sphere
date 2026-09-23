package com.adsphere.web;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
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
import org.springframework.test.web.servlet.ResultActions;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class DealBiddingApiTest {

  @Autowired private MockMvc mvc;

  private String admin;

  @Before
  public void login() throws Exception {
    admin = ApiTestSupport.admin(mvc);
  }

  private ResultActions bid(long dealId, String amount) throws Exception {
    return mvc.perform(
        post("/api/deals/" + dealId + "/bids")
            .header("Authorization", admin)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"amount\":" + amount + ",\"campaignId\":2}"));
  }

  @Test
  public void bidBelowFloorIsRejectedAndDealStaysActive() throws Exception {
    // Deal 2 "Display Standard Deal" has a floor of 8.50.
    bid(2, "8.49").andExpect(status().isOk()).andExpect(jsonPath("$.status", is("REJECTED")));
    mvc.perform(get("/api/deals/2").header("Authorization", admin))
        .andExpect(jsonPath("$.status", is("ACTIVE")));
  }

  @Test
  public void winningBidSellsDealAndBlocksFurtherBids() throws Exception {
    // Deal 3 "Mobile App Deal" has a floor of 9.75.
    bid(3, "9.75")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status", is("WON")))
        .andExpect(jsonPath("$.deal.status", is("SOLD")))
        .andExpect(jsonPath("$.deal.campaignName", is("Product Launch")))
        .andExpect(jsonPath("$.deal.winningBidder", is("admin")));

    bid(3, "50").andExpect(status().isConflict());
  }

  @Test
  public void pausedDealRejectsBids() throws Exception {
    // Deal 5 "Desktop Display Deal" is seeded as paused.
    bid(5, "100").andExpect(status().isConflict());
  }

  @Test
  public void validatesBidPayload() throws Exception {
    mvc.perform(
            post("/api/deals/1/bids")
                .header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":-1}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors.amount").exists())
        .andExpect(jsonPath("$.fieldErrors.campaignId").exists());
  }

  @Test
  public void filtersInventory() throws Exception {
    mvc.perform(get("/api/deals").param("type", "RTB").header("Authorization", admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].type", everyItem(is("RTB"))));
    mvc.perform(get("/api/deals").param("q", "podcast").header("Authorization", admin))
        .andExpect(jsonPath("$.totalElements", is(1)));
  }
}
