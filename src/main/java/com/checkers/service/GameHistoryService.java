package com.checkers.service;

import com.checkers.entity.GameHistory;
import com.checkers.entity.Profile;
import com.checkers.game.GameSession;
import com.checkers.model.PieceColor;
import com.checkers.repository.GameHistoryRepository;
import com.checkers.repository.ProfileRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameHistoryService {

  private final GameHistoryRepository gameHistoryRepository;
  private final ProfileRepository profileRepository;

  @Transactional
  public void saveFinishedGame(GameSession session, String userId) {
    if (session.getWinner() == null || session.isHistorySaved()) {
      return;
    }

    Profile profile =
        profileRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalStateException("Profile not found: " + userId));

    String result = resolveResult(session);
    updateStats(profile, result);

    GameHistory history =
        GameHistory.builder()
            .id(UUID.randomUUID().toString())
            .userId(userId)
            .opponentType("vsAI".equals(session.getMode()) ? "ai" : "human")
            .result(result)
            .moves(new ArrayList<>(session.getMoves()))
            .build();

    gameHistoryRepository.save(history);
    session.setHistorySaved(true);
    log.info("Game saved for user {} — result: {} (w:{} l:{} d:{})",
        userId, result, profile.getWins(), profile.getLosses(), profile.getDraws());
  }

  @Transactional(readOnly = true)
  public List<GameHistory> findHistoryForUser(String userId) {
    return gameHistoryRepository.findByUserIdOrderByPlayedAtDesc(userId);
  }

  private void updateStats(Profile profile, String result) {
    switch (result) {
      case "win" -> profile.setWins(profile.getWins() + 1);
      case "lose" -> profile.setLosses(profile.getLosses() + 1);
      case "draw" -> profile.setDraws(profile.getDraws() + 1);
      default -> {}
    }
    profileRepository.save(profile);
  }

  private static String resolveResult(GameSession session) {
    PieceColor winner = session.getWinner();
    if (winner == null) {
      return "draw";
    }
    return winner == session.getHumanColor() ? "win" : "lose";
  }
}
