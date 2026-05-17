package com.checkers.repository;

import com.checkers.entity.GameHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameHistoryRepository extends JpaRepository<GameHistory, String> {

  List<GameHistory> findByUserIdOrderByPlayedAtDesc(String userId);
}
