package org.example.library.controller;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.example.library.dto.auth.UserDto;
import org.example.library.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MeController {
  private final AuthService authService;

  @GetMapping("/me")
  public UserDto me(Authentication authentication) {
    return authService.me(UUID.fromString(authentication.getName()));
  }
}
