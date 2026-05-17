package com.checkers.controller.dto;

import com.checkers.model.Move;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MoveDTO {
  private int fromX;
  private int fromY;
  private int toX;
  private int toY;
  private boolean capture;
  private String player;
  private List<int[]> capturedSquares;

  public static MoveDTO from(Move move) {
    if (move == null) {
      return null;
    }
    return MoveDTO.builder()
        .fromX(move.getFromX())
        .fromY(move.getFromY())
        .toX(move.getToX())
        .toY(move.getToY())
        .capture(move.isCapture())
        .player(move.getPlayer() != null ? move.getPlayer().name() : null)
        .capturedSquares(move.getCapturedSquares())
        .build();
  }
}
