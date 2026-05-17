package com.checkers.controller;

import com.checkers.controller.dto.LoginRequest;
import com.checkers.controller.dto.ProfileResponse;
import com.checkers.controller.dto.RegisterRequest;
import com.checkers.security.SecurityContextHelper;
import com.checkers.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final SecurityContextHelper securityContextHelper;

  @PostMapping("/register")
  public ProfileResponse register(
      @RequestBody RegisterRequest request,
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse) {
    Authentication auth = authService.register(request);
    securityContextHelper.persist(auth, httpRequest, httpResponse);
    return ProfileResponse.from(((com.checkers.security.ProfileUserDetails) auth.getPrincipal()).getProfile());
  }

  @PostMapping("/login")
  public ProfileResponse login(
      @RequestBody LoginRequest request,
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse) {
    Authentication auth = authService.login(request);
    securityContextHelper.persist(auth, httpRequest, httpResponse);
    return ProfileResponse.from(((com.checkers.security.ProfileUserDetails) auth.getPrincipal()).getProfile());
  }

  @GetMapping("/me")
  public ProfileResponse me() {
    return authService.currentUser();
  }

  @PostMapping("/logout")
  public void logout(HttpServletRequest request) {
    authService.logout();
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }
  }
}
