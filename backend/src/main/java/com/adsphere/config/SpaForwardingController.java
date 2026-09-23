package com.adsphere.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * When the built frontend is bundled into the jar (under classpath:/static), client-side routes
 * such as /campaigns/new must resolve to index.html.
 */
@Controller
public class SpaForwardingController {

  @GetMapping({
    "/",
    "/login",
    "/campaigns/**",
    "/placements/**",
    "/audience/**",
    "/inventory/**",
    "/settings/**",
    "/help/**",
    "/profile/**"
  })
  public String forward() {
    return "forward:/index.html";
  }
}
