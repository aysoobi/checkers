package com.checkers.service;

import com.checkers.controller.BoardMapper;
import com.checkers.controller.dto.GameSessionDTO;
import com.checkers.controller.dto.GameStateDTO;
import com.checkers.controller.dto.MoveDTO;
import com.checkers.controller.dto.MoveRequest;
import com.checkers.controller.dto.NewGameRequest;
import com.checkers.game.GameSession;
import com.checkers.game.GameStorage;
import com.checkers.model.Board;
import com.checkers.model.Move;
import com.checkers.model.PieceColor;
import com.checkers.exception.GameErrorCode;
import com.checkers.exception.GameException;
import com.checkers.model.Piece;
import com.checkers.security.UserContext;
import com.checkers.service.ai.AiService;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GameService {

  private static final String MODE_TWO_PLAYER = "twoPlayer";
  private static final String MODE_VS_AI = "vsAI";

  private final GameStorage gameStorage;
  private final AiService aiService;
  private final GameHistoryService gameHistoryService;

  public GameSessionDTO newGame(NewGameRequest request) {
    return createGame(request);
  }

  public GameStateDTO makeMove(String gameId, MoveRequest request) {
    return applyPlayerMove(gameId, request);
  }

  public GameSessionDTO createGame(NewGameRequest request) {
    String mode = normalizeMode(request != null ? request.getMode() : null);
    String difficulty = normalizeDifficulty(request != null ? request.getAiDifficulty() : null, mode);

    Board board = Board.initialSetup();
    GameSession session =
        GameSession.builder()
            .gameId(UUID.randomUUID().toString())
            .board(board)
            .currentTurn(board.getCurrentTurn())
            .mode(mode)
            .aiDifficulty(difficulty)
            .build();

    gameStorage.save(session);
    return toSessionDto(session);
  }

  public GameStateDTO getState(String gameId) {
    return getGameState(gameId);
  }

  public GameStateDTO getGameState(String gameId) {
    GameSession session = requireSession(gameId);
    return toStateDto(session);
  }

  public GameStateDTO applyPlayerMove(String gameId, MoveRequest request) {
    GameSession session = requireSession(gameId);
    if (session.getWinner() != null) {
      throw new GameException(
          GameErrorCode.GAME_FINISHED, HttpStatus.BAD_REQUEST, "Партия уже завершена");
    }

    PieceColor turn = session.getBoard().getCurrentTurn();
    if (MODE_VS_AI.equals(session.getMode()) && turn == session.getAiColor()) {
      throw new GameException(
          GameErrorCode.WRONG_TURN,
          HttpStatus.BAD_REQUEST,
          "Сейчас ход компьютера. Подождите ответ ИИ.");
    }

    Move move = resolveMove(session.getBoard(), turn, request);
    applyMoveToSession(session, move);
    return toStateDto(session);
  }

  public List<MoveDTO> getHints(String gameId) {
    GameSession session = requireSession(gameId);
    if (session.getWinner() != null) {
      return List.of();
    }
    return CheckersService.getAllValidMoves(session.getBoard(), session.getBoard().getCurrentTurn())
        .stream()
        .map(MoveDTO::from)
        .toList();
  }

  public GameStateDTO applyAiMove(String gameId) {
    GameSession session = requireSession(gameId);
    if (!MODE_VS_AI.equals(session.getMode())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game is not in vsAI mode");
    }
    if (session.getWinner() != null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game is already finished");
    }
    if (session.getBoard().getCurrentTurn() != session.getAiColor()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "It is not AI turn");
    }

    Move aiMove =
        aiService.getBestMove(
            session.getBoard(), session.getAiColor(), session.getAiDifficulty());
    if (aiMove == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "AI has no valid moves");
    }

    applyMoveToSession(session, aiMove);
    return toStateDto(session);
  }

  private void applyMoveToSession(GameSession session, Move move) {
    Board next = CheckersService.applyMove(session.getBoard(), move);
    session.setBoard(next);
    session.setCurrentTurn(next.getCurrentTurn());
    session.setLastMove(move);
    session.getMoves().add(move);

    if (CheckersService.isGameOver(next)) {
      session.setWinner(CheckersService.getWinner(next));
      if (!session.isHistorySaved()) {
        gameHistoryService.saveFinishedGame(session, UserContext.requireUserId());
      }
    }
  }

  private Move resolveMove(Board board, PieceColor color, MoveRequest request) {
    List<Move> valid = CheckersService.getAllValidMoves(board, color);
    return valid.stream()
        .filter(m -> matchesRequest(m, request))
        .findFirst()
        .orElseThrow(() -> explainInvalidMove(board, color, request, valid));
  }

  private static GameException explainInvalidMove(
      Board board, PieceColor color, MoveRequest request, List<Move> valid) {
    String colorRu = color == PieceColor.WHITE ? "белых" : "чёрных";

    if (valid.isEmpty()) {
      return new GameException(
          GameErrorCode.INVALID_MOVE, HttpStatus.BAD_REQUEST, "Нет доступных ходов для " + colorRu);
    }

    boolean mandatoryCapture = valid.stream().anyMatch(Move::isCapture);
    Piece piece = board.getPiece(request.getFromX(), request.getFromY());

    if (piece == null) {
      return new GameException(
          GameErrorCode.INVALID_MOVE,
          HttpStatus.BAD_REQUEST,
          "На выбранной клетке нет вашей шашки.");
    }

    if (piece.getColor() != color) {
      return new GameException(
          GameErrorCode.WRONG_TURN,
          HttpStatus.BAD_REQUEST,
          "Сейчас ход " + colorRu + ", а не этой шашки.");
    }

    boolean fromHasMove =
        valid.stream()
            .anyMatch(
                m -> m.getFromX() == request.getFromX() && m.getFromY() == request.getFromY());

    if (mandatoryCapture && !fromHasMove) {
      return new GameException(
          GameErrorCode.MANDATORY_CAPTURE,
          HttpStatus.BAD_REQUEST,
          "Обязательное взятие! Другая шашка должна взять соперника. "
              + "Вы не можете ходить этой фигурой, пока есть взятие.");
    }

    if (!fromHasMove) {
      return new GameException(
          GameErrorCode.PIECE_CANNOT_MOVE,
          HttpStatus.BAD_REQUEST,
          "Эта шашка не может так ходить. Шашки ходят только по диагонали на одну клетку вперёд "
              + "(простая) или на любое расстояние по диагонали (дамка).");
    }

    return new GameException(
        GameErrorCode.INVALID_MOVE,
        HttpStatus.BAD_REQUEST,
        "Такой ход невозможен. Проверьте диагональ и правила взятия.");
  }

  private static boolean matchesRequest(Move move, MoveRequest request) {
    return move.getFromX() == request.getFromX()
        && move.getFromY() == request.getFromY()
        && move.getToX() == request.getToX()
        && move.getToY() == request.getToY();
  }

  private GameSession requireSession(String gameId) {
    return gameStorage
        .find(gameId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
  }

  private static String normalizeMode(String mode) {
    if (mode == null || mode.isBlank()) {
      return MODE_TWO_PLAYER;
    }
    if (MODE_TWO_PLAYER.equals(mode) || MODE_VS_AI.equals(mode)) {
      return mode;
    }
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown mode: " + mode);
  }

  private static String normalizeDifficulty(String difficulty, String mode) {
    if (!MODE_VS_AI.equals(mode)) {
      return null;
    }
    if (difficulty == null || difficulty.isBlank()) {
      return "MEDIUM";
    }
    String upper = difficulty.toUpperCase();
    if (Objects.equals(upper, "EASY") || Objects.equals(upper, "MEDIUM") || Objects.equals(upper, "HARD")) {
      return upper;
    }
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown difficulty: " + difficulty);
  }

  private static GameSessionDTO toSessionDto(GameSession session) {
    return GameSessionDTO.builder()
        .gameId(session.getGameId())
        .board(BoardMapper.toMatrix(session.getBoard()))
        .currentTurn(session.getCurrentTurn().name())
        .build();
  }

  private static GameStateDTO toStateDto(GameSession session) {
    return GameStateDTO.builder()
        .gameId(session.getGameId())
        .board(BoardMapper.toMatrix(session.getBoard()))
        .currentTurn(session.getCurrentTurn().name())
        .winner(session.getWinner() != null ? session.getWinner().name() : null)
        .lastMove(MoveDTO.from(session.getLastMove()))
        .build();
  }
}
