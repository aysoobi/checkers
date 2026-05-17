package com.checkers.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Move {
  private int fromX;
  private int fromY;
  private int toX;
  private int toY;
  private boolean isCapture;
  private PieceColor player;

  @Builder.Default private List<int[]> capturedSquares = new ArrayList<>();

  public List<int[]> getCapturedSquares() {
    return capturedSquares == null
        ? Collections.emptyList()
        : Collections.unmodifiableList(capturedSquares);
  }
}
