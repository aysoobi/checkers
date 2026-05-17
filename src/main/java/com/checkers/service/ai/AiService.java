package com.checkers.service.ai;

import com.checkers.model.Board;
import com.checkers.model.Move;
import com.checkers.model.PieceColor;
import com.checkers.service.CheckersService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;

@Service
public class AiService {

  public Move getBestMove(Board board, PieceColor aiColor, String difficulty) {
    List<Move> moves = new ArrayList<>(CheckersService.getAllValidMoves(board, aiColor));
    if (moves.isEmpty()) {
      return null;
    }

    String level = difficulty == null ? "MEDIUM" : difficulty.toUpperCase();
    return switch (level) {
      case "EASY" -> moves.get(ThreadLocalRandom.current().nextInt(moves.size()));
      case "HARD" -> MiniMax.findBestMove(board, aiColor, 4, true);
      default -> MiniMax.findBestMove(board, aiColor, 2, false);
    };
  }
}
