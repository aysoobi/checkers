package com.checkers.controller;

import com.checkers.controller.dto.GameHistoryDTO;
import com.checkers.security.UserContext;
import com.checkers.service.GameHistoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

  private final GameHistoryService gameHistoryService;

  @GetMapping
  public List<GameHistoryDTO> list() {
    return gameHistoryService.findHistoryForUser(UserContext.requireUserId()).stream()
        .map(GameHistoryDTO::from)
        .toList();
  }
}
