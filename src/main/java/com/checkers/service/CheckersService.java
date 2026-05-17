package com.checkers.service;

import com.checkers.model.Board;
import com.checkers.model.Move;
import com.checkers.model.Piece;
import com.checkers.model.PieceColor;
import com.checkers.model.PieceType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CheckersService {

  private static final int[][] DIAGONALS = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

  private CheckersService() {}

  public static List<Move> getAllValidMoves(Board board, PieceColor color) {
    List<Move> captures = new ArrayList<>();
    for (int x = 0; x < 8; x++) {
      for (int y = 0; y < 8; y++) {
        Piece piece = board.getPiece(x, y);
        if (piece != null && piece.getColor() == color) {
          searchCaptures(
              board, x, y, x, y, piece, color, new ArrayList<>(), new HashSet<>(), captures);
        }
      }
    }
    if (!captures.isEmpty()) {
      return filterMaxCaptures(captures);
    }

    List<Move> quiet = new ArrayList<>();
    for (int x = 0; x < 8; x++) {
      for (int y = 0; y < 8; y++) {
        Piece piece = board.getPiece(x, y);
        if (piece != null && piece.getColor() == color) {
          quiet.addAll(getQuietMoves(board, x, y, piece));
        }
      }
    }
    return quiet;
  }

  public static Board applyMove(Board board, Move move) {
    Board next = board.copy();
    Piece moving = next.getPiece(move.getFromX(), move.getFromY());
    if (moving == null) {
      throw new IllegalArgumentException("No piece at move origin");
    }

    for (int[] square : move.getCapturedSquares()) {
      next.setPiece(square[0], square[1], null);
    }

    next.setPiece(move.getFromX(), move.getFromY(), null);
    Piece landed = promoteIfNeeded(moving, move.getToY());
    next.setPiece(move.getToX(), move.getToY(), landed);
    next.setCurrentTurn(next.getCurrentTurn().opponent());
    return next;
  }

  public static boolean isGameOver(Board board) {
    return getAllValidMoves(board, board.getCurrentTurn()).isEmpty();
  }

  public static PieceColor getWinner(Board board) {
    if (!isGameOver(board)) {
      return null;
    }
    return board.getCurrentTurn().opponent();
  }

  private static void searchCaptures(
      Board board,
      int startX,
      int startY,
      int x,
      int y,
      Piece piece,
      PieceColor color,
      List<int[]> capturedSoFar,
      Set<String> capturedKeys,
      List<Move> out) {

    List<CaptureStep> steps = findCaptureSteps(board, x, y, piece, capturedKeys);
    if (steps.isEmpty()) {
      if (!capturedSoFar.isEmpty()) {
        out.add(buildCaptureMove(startX, startY, x, y, color, capturedSoFar));
      }
      return;
    }

    for (CaptureStep step : steps) {
      Board nextBoard = board.copy();
      nextBoard.setPiece(step.capturedX(), step.capturedY(), null);
      nextBoard.setPiece(x, y, null);

      Piece moved = promoteIfNeeded(piece, step.landY());
      nextBoard.setPiece(step.landX(), step.landY(), moved);

      List<int[]> nextCaptured = new ArrayList<>(capturedSoFar);
      nextCaptured.add(new int[] {step.capturedX(), step.capturedY()});
      Set<String> nextKeys = new HashSet<>(capturedKeys);
      nextKeys.add(key(step.capturedX(), step.capturedY()));

      searchCaptures(
          nextBoard,
          startX,
          startY,
          step.landX(),
          step.landY(),
          moved,
          color,
          nextCaptured,
          nextKeys,
          out);
    }
  }

  private static List<CaptureStep> findCaptureSteps(
      Board board, int x, int y, Piece piece, Set<String> alreadyCaptured) {
    List<CaptureStep> steps = new ArrayList<>();
    if (piece.getType() == PieceType.MAN) {
      for (int[] dir : DIAGONALS) {
        int ox = x + dir[0];
        int oy = y + dir[1];
        int lx = x + 2 * dir[0];
        int ly = y + 2 * dir[1];
        if (isManCapture(board, piece, alreadyCaptured, ox, oy, lx, ly)) {
          steps.add(new CaptureStep(ox, oy, lx, ly));
        }
      }
    } else {
      for (int[] dir : DIAGONALS) {
        steps.addAll(findKingCaptureSteps(board, x, y, piece, dir[0], dir[1], alreadyCaptured));
      }
    }
    return steps;
  }

  private static List<CaptureStep> findKingCaptureSteps(
      Board board,
      int x,
      int y,
      Piece piece,
      int dx,
      int dy,
      Set<String> alreadyCaptured) {
    List<CaptureStep> steps = new ArrayList<>();
    int cx = x + dx;
    int cy = y + dy;
    boolean seenOpponent = false;
    int opponentX = -1;
    int opponentY = -1;

    while (Board.inBounds(cx, cy)) {
      Piece at = board.getPiece(cx, cy);
      if (at == null) {
        if (seenOpponent) {
          steps.add(new CaptureStep(opponentX, opponentY, cx, cy));
        }
      } else if (at.getColor() == piece.getColor()) {
        break;
      } else if (seenOpponent || alreadyCaptured.contains(key(cx, cy))) {
        break;
      } else {
        seenOpponent = true;
        opponentX = cx;
        opponentY = cy;
      }
      cx += dx;
      cy += dy;
    }
    return steps;
  }

  private static boolean isManCapture(
      Board board,
      Piece piece,
      Set<String> alreadyCaptured,
      int ox,
      int oy,
      int lx,
      int ly) {
    if (!Board.isPlayableSquare(lx, ly) || board.getPiece(lx, ly) != null) {
      return false;
    }
    Piece opponent = board.getPiece(ox, oy);
    if (opponent == null
        || opponent.getColor() == piece.getColor()
        || alreadyCaptured.contains(key(ox, oy))) {
      return false;
    }
    return Board.isPlayableSquare(ox, oy);
  }

  private static List<Move> getQuietMoves(Board board, int x, int y, Piece piece) {
    List<Move> moves = new ArrayList<>();
    if (piece.getType() == PieceType.MAN) {
      int dy = forwardDy(piece.getColor());
      for (int dx : new int[] {1, -1}) {
        int nx = x + dx;
        int ny = y + dy;
        if (canLandQuiet(board, nx, ny)) {
          moves.add(buildQuietMove(x, y, nx, ny, piece.getColor()));
        }
      }
    } else {
      for (int[] dir : DIAGONALS) {
        int nx = x + dir[0];
        int ny = y + dir[1];
        while (canLandQuiet(board, nx, ny)) {
          moves.add(buildQuietMove(x, y, nx, ny, piece.getColor()));
          nx += dir[0];
          ny += dir[1];
        }
      }
    }
    return moves;
  }

  private static boolean canLandQuiet(Board board, int x, int y) {
    return Board.isPlayableSquare(x, y) && board.getPiece(x, y) == null;
  }

  private static Piece promoteIfNeeded(Piece piece, int landY) {
    if (piece.getType() == PieceType.MAN && landY == promotionRow(piece.getColor())) {
      return Piece.builder().type(PieceType.KING).color(piece.getColor()).build();
    }
    return Piece.builder().type(piece.getType()).color(piece.getColor()).build();
  }

  private static int forwardDy(PieceColor color) {
    return color == PieceColor.WHITE ? -1 : 1;
  }

  private static int promotionRow(PieceColor color) {
    return color == PieceColor.WHITE ? 0 : 7;
  }

  private static String key(int x, int y) {
    return x + "," + y;
  }

  private static Move buildQuietMove(int fromX, int fromY, int toX, int toY, PieceColor player) {
    return Move.builder()
        .fromX(fromX)
        .fromY(fromY)
        .toX(toX)
        .toY(toY)
        .isCapture(false)
        .player(player)
        .build();
  }

  private static Move buildCaptureMove(
      int fromX, int fromY, int toX, int toY, PieceColor player, List<int[]> captured) {
    return Move.builder()
        .fromX(fromX)
        .fromY(fromY)
        .toX(toX)
        .toY(toY)
        .isCapture(true)
        .player(player)
        .capturedSquares(new ArrayList<>(captured))
        .build();
  }

  private static List<Move> filterMaxCaptures(List<Move> captures) {
    int max = captures.stream().mapToInt(m -> m.getCapturedSquares().size()).max().orElse(0);
    return captures.stream().filter(m -> m.getCapturedSquares().size() == max).toList();
  }

  private record CaptureStep(int capturedX, int capturedY, int landX, int landY) {}
}
