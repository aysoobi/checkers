package com.checkers.service.ai;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.checkers.model.Board;
import com.checkers.model.Piece;
import com.checkers.model.PieceColor;
import com.checkers.model.PieceType;
import com.checkers.model.Move;
import com.checkers.service.CheckersService;
import java.util.List;
import org.junit.jupiter.api.Test;

class AiServiceTest {

  private final AiService aiService = new AiService();

  @Test
  void easyReturnsValidMove() {
    Board board = Board.initialSetup();
    Move move = aiService.getBestMove(board, PieceColor.BLACK, "EASY");
    assertNotNull(move);
    assertTrue(isValid(board, PieceColor.BLACK, move));
  }

  @Test
  void mediumReturnsValidMove() {
    Board board = Board.initialSetup();
    Move move = aiService.getBestMove(board, PieceColor.BLACK, "MEDIUM");
    assertNotNull(move);
    assertTrue(isValid(board, PieceColor.BLACK, move));
  }

  @Test
  void hardReturnsValidMove() {
    Board board = Board.initialSetup();
    Move move = aiService.getBestMove(board, PieceColor.BLACK, "HARD");
    assertNotNull(move);
    assertTrue(isValid(board, PieceColor.BLACK, move));
  }

  @Test
  void prefersMaxCaptureWhenAvailable() {
    Board board = new Board();
    board.setCurrentTurn(PieceColor.BLACK);
    board.setPiece(2, 5, piece(PieceColor.WHITE));
    board.setPiece(3, 4, piece(PieceColor.BLACK));
    board.setPiece(4, 3, piece(PieceColor.WHITE));
    board.setPiece(5, 2, piece(PieceColor.WHITE));

    Move move = aiService.getBestMove(board, PieceColor.BLACK, "MEDIUM");
    assertNotNull(move);
    assertTrue(move.isCapture());
    assertTrue(move.getCapturedSquares().size() >= 1);
  }

  private static boolean isValid(Board board, PieceColor color, Move move) {
    List<Move> moves = CheckersService.getAllValidMoves(board, color);
    return moves.stream()
        .anyMatch(
            m ->
                m.getFromX() == move.getFromX()
                    && m.getFromY() == move.getFromY()
                    && m.getToX() == move.getToX()
                    && m.getToY() == move.getToY());
  }

  private static Piece piece(PieceColor color) {
    return Piece.builder().color(color).type(PieceType.MAN).build();
  }
}
