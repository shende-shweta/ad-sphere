package com.adsphere.web;

import com.adsphere.dto.FaqResponse;
import com.adsphere.dto.SupportTicketRequest;
import com.adsphere.service.HelpService;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/help")
public class HelpController {

  private final HelpService service;

  public HelpController(HelpService service) {
    this.service = service;
  }

  @GetMapping("/faqs")
  public List<FaqResponse> faqs() {
    return service.faqs();
  }

  @PostMapping("/tickets")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Long> createTicket(
      @Valid @RequestBody SupportTicketRequest request, Principal principal) {
    return Collections.singletonMap("id", service.createTicket(principal.getName(), request));
  }
}
