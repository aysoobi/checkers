package com.checkers.service;

import com.checkers.controller.dto.LoginRequest;
import com.checkers.controller.dto.ProfileResponse;
import com.checkers.controller.dto.RegisterRequest;
import com.checkers.entity.Profile;
import com.checkers.repository.ProfileRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final ProfileRepository profileRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;

  public Authentication register(RegisterRequest request) {
    if (request.getUsername() == null || request.getUsername().trim().length() < 3) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Логин минимум 3 символа");
    }
    if (request.getPassword() == null || request.getPassword().length() < 6) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пароль минимум 6 символов");
    }
    String username = request.getUsername().trim().toLowerCase();
    if (profileRepository.existsByUsername(username)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Такой логин уже занят");
    }

    Profile profile =
        Profile.builder()
            .userId(UUID.randomUUID().toString())
            .username(username)
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .city(request.getCity())
            .build();
    profileRepository.save(profile);
    return authenticate(username, request.getPassword());
  }

  public Authentication login(LoginRequest request) {
    if (request.getUsername() == null || request.getPassword() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Введите логин и пароль");
    }
    String username = request.getUsername().trim().toLowerCase();
    try {
      return authenticate(username, request.getPassword());
    } catch (BadCredentialsException e) {
      throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED, "Неверный логин или пароль");
    }
  }

  public ProfileResponse currentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof com.checkers.security.ProfileUserDetails details)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Не авторизован");
    }
    return ProfileResponse.from(details.getProfile());
  }

  public void logout() {
    SecurityContextHolder.clearContext();
  }

  private Authentication authenticate(String username, String password) {
    return authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(username, password));
  }
}
