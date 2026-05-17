package com.checkers.service;

import com.checkers.controller.dto.ProfileResponse;
import com.checkers.entity.Profile;
import com.checkers.repository.ProfileRepository;
import com.checkers.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProfileService {

  private final ProfileRepository profileRepository;

  @Transactional
  public ProfileResponse updateCity(String city) {
    Profile profile =
        profileRepository
            .findById(UserContext.requireUserId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Профиль не найден"));
    profile.setCity(city);
    return ProfileResponse.from(profileRepository.save(profile));
  }
}
