package com.checkers.controller.dto;

import lombok.Data;

@Data
public class NewGameRequest {
  private String mode;
  private String aiDifficulty;
}
