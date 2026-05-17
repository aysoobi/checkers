package com.checkers.controller;

import com.checkers.controller.dto.ProfileResponse;
import com.checkers.service.AuthService;
import com.checkers.service.ProfileService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

  private final AuthService authService;
  private final ProfileService profileService;

  @GetMapping
  public ProfileResponse getProfile() {
    return authService.currentUser();
  }

  @PutMapping
  public ProfileResponse updateProfile(@RequestBody Map<String, String> body) {
    return profileService.updateCity(body.get("city"));
  }
}
