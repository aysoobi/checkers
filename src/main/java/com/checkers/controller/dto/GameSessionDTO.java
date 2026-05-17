package com.checkers.controller.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameSessionDTO {
  private String gameId;
  private int[][] board;
  private String currentTurn;
}
