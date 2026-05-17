package com.checkers.game;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class GameStorage {

  private final ConcurrentHashMap<String, GameSession> sessions = new ConcurrentHashMap<>();

  public void save(GameSession session) {
    sessions.put(session.getGameId(), session);
  }

  public Optional<GameSession> find(String gameId) {
    return Optional.ofNullable(sessions.get(gameId));
  }

  public void remove(String gameId) {
    sessions.remove(gameId);
  }
}
