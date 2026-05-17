package com.checkers.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Board {

  private static final int SIZE = 8;

  @Builder.Default private Piece[][] cells = new Piece[SIZE][SIZE];

  @Builder.Default private PieceColor currentTurn = PieceColor.WHITE;

  public Piece getPiece(int x, int y) {
    if (!inBounds(x, y)) {
      return null;
    }
    return cells[x][y];
  }

  public void setPiece(int x, int y, Piece piece) {
    if (!inBounds(x, y)) {
      return;
    }
    cells[x][y] = piece;
  }

  public Board copy() {
    Piece[][] copied = new Piece[SIZE][SIZE];
    for (int x = 0; x < SIZE; x++) {
      for (int y = 0; y < SIZE; y++) {
        Piece p = cells[x][y];
        copied[x][y] =
            p == null
                ? null
                : Piece.builder().type(p.getType()).color(p.getColor()).build();
      }
    }
    return Board.builder().cells(copied).currentTurn(currentTurn).build();
  }

  public static boolean isPlayableSquare(int x, int y) {
    return inBounds(x, y) && (x + y) % 2 == 1;
  }

  public static boolean inBounds(int x, int y) {
    return x >= 0 && x < SIZE && y >= 0 && y < SIZE;
  }

  public static Board initialSetup() {
    Board board = new Board();
    for (int y = 0; y < SIZE; y++) {
      for (int x = 0; x < SIZE; x++) {
        if (!isPlayableSquare(x, y)) {
          continue;
        }
        if (y <= 2) {
          board.setPiece(
              x, y, Piece.builder().color(PieceColor.BLACK).type(PieceType.MAN).build());
        } else if (y >= 5) {
          board.setPiece(
              x, y, Piece.builder().color(PieceColor.WHITE).type(PieceType.MAN).build());
        }
      }
    }
    return board;
  }
}
