package com.checkers.controller.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameStateDTO {
  private String gameId;
  private int[][] board;
  private String currentTurn;
  private String winner;
  private MoveDTO lastMove;
}
