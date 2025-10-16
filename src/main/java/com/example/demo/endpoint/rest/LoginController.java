package com.example.demo.endpoint.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

  @GetMapping("/")
  public String home() {
    return "login";
  }

  @GetMapping("/welcome")
  public String welcome() {
    return "welcome";
  }
}
