package com.checkers.controller;

import com.checkers.model.Board;
import com.checkers.model.Piece;
import com.checkers.model.PieceColor;
import com.checkers.model.PieceType;

public final class BoardMapper {

  public static final int EMPTY = 0;
  public static final int WHITE_MAN = 1;
  public static final int WHITE_KING = 2;
  public static final int BLACK_MAN = 3;
  public static final int BLACK_KING = 4;

  private BoardMapper() {}

  public static int[][] toMatrix(Board board) {
    int[][] matrix = new int[8][8];
    for (int x = 0; x < 8; x++) {
      for (int y = 0; y < 8; y++) {
        matrix[x][y] = encode(board.getPiece(x, y));
      }
    }
    return matrix;
  }

  private static int encode(Piece piece) {
    if (piece == null) {
      return EMPTY;
    }
    boolean white = piece.getColor() == PieceColor.WHITE;
    boolean king = piece.getType() == PieceType.KING;
    if (white) {
      return king ? WHITE_KING : WHITE_MAN;
    }
    return king ? BLACK_KING : BLACK_MAN;
  }
}
