package com.checkers.game;

import com.checkers.model.Board;
import com.checkers.model.Move;
import com.checkers.model.PieceColor;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameSession {

  private String gameId;
  private Board board;
  private PieceColor currentTurn;
  private PieceColor winner;
  private String mode;
  private String aiDifficulty;

  @Builder.Default private PieceColor humanColor = PieceColor.WHITE;

  @Builder.Default private PieceColor aiColor = PieceColor.BLACK;

  @Builder.Default private List<Move> moves = new ArrayList<>();

  private Move lastMove;

  @Builder.Default private boolean historySaved = false;
}
