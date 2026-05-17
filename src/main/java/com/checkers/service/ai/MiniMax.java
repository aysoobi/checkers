package com.checkers.service.ai;

import com.checkers.model.Board;
import com.checkers.model.Move;
import com.checkers.model.Piece;
import com.checkers.model.PieceColor;
import com.checkers.model.PieceType;
import com.checkers.service.CheckersService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class MiniMax {

  private static final int MAN_VALUE = 1;
  private static final int KING_VALUE = 3;
  private static final int CAPTURE_BONUS = 50;

  private MiniMax() {}

  public static int evaluateBoard(Board board, PieceColor aiColor, boolean centerBonus) {
    int score = 0;
    for (int x = 0; x < 8; x++) {
      for (int y = 0; y < 8; y++) {
        Piece piece = board.getPiece(x, y);
        if (piece == null) {
          continue;
        }
        int value = piece.getType() == PieceType.KING ? KING_VALUE : MAN_VALUE;
        if (centerBonus && isCenter(x, y)) {
          value += 1;
        }
        score += piece.getColor() == aiColor ? value : -value;
      }
    }
    return score;
  }

  public static Move findBestMove(Board board, PieceColor aiColor, int depth, boolean centerBonus) {
    List<Move> moves = new ArrayList<>(CheckersService.getAllValidMoves(board, aiColor));
    if (moves.isEmpty()) {
      return null;
    }
    if (moves.size() == 1 || depth <= 0) {
      return pickBestCapture(moves);
    }

    moves.sort(captureComparator().reversed());

    Move bestMove = null;
    int bestScore = Integer.MIN_VALUE;
    for (Move move : moves) {
      Board next = CheckersService.applyMove(board, move);
      int score =
          minScore(next, aiColor, depth - 1, centerBonus, Integer.MIN_VALUE, Integer.MAX_VALUE);
      score += captureHeuristic(move);
      if (score > bestScore) {
        bestScore = score;
        bestMove = move;
      }
    }
    return bestMove != null ? bestMove : pickBestCapture(moves);
  }

  private static int minScore(
      Board board,
      PieceColor aiColor,
      int depth,
      boolean centerBonus,
      int alpha,
      int beta) {
    if (depth == 0 || CheckersService.isGameOver(board)) {
      return evaluateBoard(board, aiColor, centerBonus);
    }

    PieceColor opponent = aiColor.opponent();
    List<Move> moves = new ArrayList<>(CheckersService.getAllValidMoves(board, opponent));
    if (moves.isEmpty()) {
      return evaluateBoard(board, aiColor, centerBonus);
    }

    moves.sort(captureComparator());

    int min = Integer.MAX_VALUE;
    for (Move move : moves) {
      Board next = CheckersService.applyMove(board, move);
      int score =
          maxScore(next, aiColor, depth - 1, centerBonus, alpha, beta) - captureHeuristic(move);
      min = Math.min(min, score);
      beta = Math.min(beta, min);
      if (beta <= alpha) {
        break;
      }
    }
    return min;
  }

  private static int maxScore(
      Board board,
      PieceColor aiColor,
      int depth,
      boolean centerBonus,
      int alpha,
      int beta) {
    if (depth == 0 || CheckersService.isGameOver(board)) {
      return evaluateBoard(board, aiColor, centerBonus);
    }

    List<Move> moves = new ArrayList<>(CheckersService.getAllValidMoves(board, aiColor));
    if (moves.isEmpty()) {
      return evaluateBoard(board, aiColor, centerBonus);
    }

    moves.sort(captureComparator().reversed());

    int max = Integer.MIN_VALUE;
    for (Move move : moves) {
      Board next = CheckersService.applyMove(board, move);
      int score =
          minScore(next, aiColor, depth - 1, centerBonus, alpha, beta) + captureHeuristic(move);
      max = Math.max(max, score);
      alpha = Math.max(alpha, max);
      if (beta <= alpha) {
        break;
      }
    }
    return max;
  }

  private static int captureHeuristic(Move move) {
    if (!move.isCapture()) {
      return 0;
    }
    return CAPTURE_BONUS * move.getCapturedSquares().size();
  }

  private static Move pickBestCapture(List<Move> moves) {
    return moves.stream().max(captureComparator()).orElse(moves.get(0));
  }

  private static Comparator<Move> captureComparator() {
    return Comparator.comparingInt(m -> m.getCapturedSquares().size());
  }

  private static boolean isCenter(int x, int y) {
    return x >= 3 && x <= 6 && y >= 3 && y <= 6;
  }
}
