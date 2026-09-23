package com.adsphere.service;

import com.adsphere.domain.SupportTicket;
import com.adsphere.dto.FaqResponse;
import com.adsphere.dto.SupportTicketRequest;
import com.adsphere.repository.SupportTicketRepository;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HelpService {

  private static final List<FaqResponse> FAQS =
      Arrays.asList(
          new FaqResponse(
              "Campaigns",
              "How do I create a campaign?",
              "Open Campaigns and click Create Campaign. Fill in the name, objective, flight dates,"
                  + " budget and status, then assign existing placements or create a new one."),
          new FaqResponse(
              "Campaigns",
              "Can I change a campaign after it is live?",
              "Yes. Open the campaign's actions menu and choose Edit. Budget and end date changes"
                  + " apply immediately."),
          new FaqResponse(
              "Placements",
              "What is a placement?",
              "A placement defines where and to whom ads are shown: country, audience group,"
                  + " traffic type (app or website), ad position, device and format."),
          new FaqResponse(
              "Inventory",
              "How does bidding work?",
              "Every deal has a floor CPM. Place a bid at or above the floor and the deal is sold"
                  + " to your campaign right away; its ads start serving on that inventory. Bids"
                  + " below the floor are rejected."),
          new FaqResponse(
              "Inventory",
              "Why can't I bid on a deal?",
              "Paused and sold deals do not accept bids, and viewers have read-only access."),
          new FaqResponse(
              "Account",
              "What do the roles mean?",
              "Administrators manage everything including settings. Campaign managers create"
                  + " campaigns, placements and bids. Viewers have read-only access."),
          new FaqResponse(
              "Account",
              "How do I change my password?",
              "Go to Profile, open Security and enter your current and new password."));

  private final SupportTicketRepository tickets;

  public HelpService(SupportTicketRepository tickets) {
    this.tickets = tickets;
  }

  public List<FaqResponse> faqs() {
    return FAQS;
  }

  @Transactional
  public Long createTicket(String requester, SupportTicketRequest request) {
    return tickets
        .save(new SupportTicket(requester, request.subject.trim(), request.message.trim()))
        .getId();
  }
}
