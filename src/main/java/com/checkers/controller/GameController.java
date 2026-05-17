package com.checkers.controller;

import com.checkers.controller.dto.GameSessionDTO;
import com.checkers.controller.dto.GameStateDTO;
import com.checkers.controller.dto.MoveDTO;
import com.checkers.controller.dto.MoveRequest;
import com.checkers.controller.dto.NewGameRequest;
import com.checkers.service.GameService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {

  private final GameService gameService;

  @PostMapping("/new")
  public GameSessionDTO createGame(@RequestBody(required = false) NewGameRequest request) {
    return gameService.createGame(request);
  }

  @GetMapping("/{gameId}/state")
  public GameStateDTO getState(@PathVariable String gameId) {
    return gameService.getState(gameId);
  }

  @PostMapping("/{gameId}/move")
  public GameStateDTO move(@PathVariable String gameId, @RequestBody MoveRequest request) {
    return gameService.applyPlayerMove(gameId, request);
  }

  @PostMapping("/{gameId}/ai-move")
  public GameStateDTO aiMove(@PathVariable String gameId) {
    return gameService.applyAiMove(gameId);
  }

  @GetMapping("/{gameId}/hints")
  public List<MoveDTO> hints(@PathVariable String gameId) {
    return gameService.getHints(gameId);
  }
}
