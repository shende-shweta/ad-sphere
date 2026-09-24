package com.adsphere.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardingController {

  @GetMapping({
    "/",
    "/login",
    "/campaigns/**",
    "/placements/**",
    "/audience/**",
    "/inventory/**",
    "/activity/**",
    "/settings/**",
    "/help/**",
    "/profile/**"
  })
  public String forward() {
    return "forward:/index.html";
  }
}
