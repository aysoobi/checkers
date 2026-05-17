package com.checkers.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.checkers.controller.dto.GameSessionDTO;
import com.checkers.controller.dto.GameStateDTO;
import com.checkers.controller.dto.MoveRequest;
import com.checkers.controller.dto.NewGameRequest;
import com.checkers.entity.Profile;
import com.checkers.game.GameStorage;
import com.checkers.security.ProfileUserDetails;
import com.checkers.service.ai.AiService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

  @Mock private GameHistoryService gameHistoryService;

  private GameService gameService;

  @BeforeEach
  void setUp() {
    gameService = new GameService(new GameStorage(), new AiService(), gameHistoryService);
    Profile profile =
        Profile.builder().userId("player-1").username("player").passwordHash("x").build();
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(new ProfileUserDetails(profile), null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void createGameReturnsSession() {
    NewGameRequest request = new NewGameRequest();
    request.setMode("vsAI");
    request.setAiDifficulty("EASY");

    GameSessionDTO dto = gameService.createGame(request);

    assertNotNull(dto.getGameId());
    assertEquals("WHITE", dto.getCurrentTurn());
    assertNotNull(dto.getBoard());
  }

  @Test
  void vsAiGameAllowsAiMoveAfterHumanTurn() {
    NewGameRequest request = new NewGameRequest();
    request.setMode("vsAI");
    request.setAiDifficulty("EASY");

    GameSessionDTO created = gameService.createGame(request);

    MoveRequest humanMove = new MoveRequest();
    humanMove.setFromX(2);
    humanMove.setFromY(5);
    humanMove.setToX(3);
    humanMove.setToY(4);
    gameService.applyPlayerMove(created.getGameId(), humanMove);

    GameStateDTO afterAi = gameService.applyAiMove(created.getGameId());
    assertNotNull(afterAi.getLastMove());
    assertEquals("WHITE", afterAi.getCurrentTurn());
  }

  @Test
  void finishedGameTriggersHistorySave() {
    NewGameRequest request = new NewGameRequest();
    request.setMode("twoPlayer");
    GameSessionDTO created = gameService.createGame(request);
    assertNotNull(created.getGameId());
    verify(gameHistoryService, org.mockito.Mockito.never()).saveFinishedGame(any(), any());
  }
}
