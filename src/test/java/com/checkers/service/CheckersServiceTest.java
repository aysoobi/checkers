package com.checkers.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.checkers.model.Board;
import com.checkers.model.Move;
import com.checkers.model.Piece;
import com.checkers.model.PieceColor;
import com.checkers.model.PieceType;
import java.util.List;
import org.junit.jupiter.api.Test;

class CheckersServiceTest {

  @Test
  void quietMoveForWhiteMan() {
    Board board = emptyBoard();
    board.setCurrentTurn(PieceColor.WHITE);
    board.setPiece(1, 6, man(PieceColor.WHITE));

    List<Move> moves = CheckersService.getAllValidMoves(board, PieceColor.WHITE);

    assertEquals(2, moves.size());
    assertTrue(moves.stream().noneMatch(Move::isCapture));
    assertTrue(moves.stream().anyMatch(m -> m.getToX() == 0 && m.getToY() == 5));
    assertTrue(moves.stream().anyMatch(m -> m.getToX() == 2 && m.getToY() == 5));
  }

  @Test
  void manCaptureRemovesOpponent() {
    Board board = emptyBoard();
    board.setCurrentTurn(PieceColor.WHITE);
    board.setPiece(2, 5, man(PieceColor.WHITE));
    board.setPiece(3, 4, man(PieceColor.BLACK));

    List<Move> moves = CheckersService.getAllValidMoves(board, PieceColor.WHITE);
    assertEquals(1, moves.size());
    Move capture = moves.get(0);
    assertTrue(capture.isCapture());
    assertEquals(1, capture.getCapturedSquares().size());
    assertEquals(3, capture.getCapturedSquares().get(0)[0]);
    assertEquals(4, capture.getCapturedSquares().get(0)[1]);

    Board after = CheckersService.applyMove(board, capture);
    assertEquals(null, after.getPiece(3, 4));
    assertEquals(PieceType.MAN, after.getPiece(4, 3).getType());
    assertEquals(PieceColor.BLACK, after.getCurrentTurn());
  }

  @Test
  void promotionToKingOnLastRow() {
    Board board = emptyBoard();
    board.setCurrentTurn(PieceColor.WHITE);
    board.setPiece(2, 1, man(PieceColor.WHITE));

    Move move = Move.builder()
        .fromX(2)
        .fromY(1)
        .toX(1)
        .toY(0)
        .isCapture(false)
        .player(PieceColor.WHITE)
        .build();

    Board after = CheckersService.applyMove(board, move);
    Piece landed = after.getPiece(1, 0);
    assertEquals(PieceType.KING, landed.getType());
    assertEquals(PieceColor.WHITE, landed.getColor());
  }

  @Test
  void mandatoryCaptureBlocksQuietMoves() {
    Board board = emptyBoard();
    board.setCurrentTurn(PieceColor.WHITE);
    board.setPiece(1, 4, man(PieceColor.WHITE));
    board.setPiece(2, 3, man(PieceColor.BLACK));
    board.setPiece(4, 5, man(PieceColor.WHITE));

    List<Move> moves = CheckersService.getAllValidMoves(board, PieceColor.WHITE);
    assertFalse(moves.isEmpty());
    assertTrue(moves.stream().allMatch(Move::isCapture));
  }

  @Test
  void gameOverAndWinner() {
    Board board = emptyBoard();
    board.setCurrentTurn(PieceColor.WHITE);
    board.setPiece(1, 2, man(PieceColor.WHITE));

    assertFalse(CheckersService.isGameOver(board));
    assertEquals(null, CheckersService.getWinner(board));

    board.setCurrentTurn(PieceColor.BLACK);
    assertTrue(CheckersService.isGameOver(board));
    assertEquals(PieceColor.WHITE, CheckersService.getWinner(board));
  }

  private static Board emptyBoard() {
    return new Board();
  }

  private static Piece man(PieceColor color) {
    return Piece.builder().color(color).type(PieceType.MAN).build();
  }
}
