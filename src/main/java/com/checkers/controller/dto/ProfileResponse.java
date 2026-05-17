package com.checkers.controller.dto;

import com.checkers.entity.Profile;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponse {
  private String userId;
  private String username;
  private String city;
  private int wins;
  private int losses;
  private int draws;

  public static ProfileResponse from(Profile profile) {
    return ProfileResponse.builder()
        .userId(profile.getUserId())
        .username(profile.getUsername())
        .city(profile.getCity())
        .wins(profile.getWins())
        .losses(profile.getLosses())
        .draws(profile.getDraws())
        .build();
  }
}
