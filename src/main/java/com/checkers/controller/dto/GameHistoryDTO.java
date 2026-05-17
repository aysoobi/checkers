package com.checkers.controller.dto;

import com.checkers.entity.GameHistory;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameHistoryDTO {
  private String id;
  private String opponentType;
  private String result;
  private int moveCount;
  private OffsetDateTime playedAt;

  public static GameHistoryDTO from(GameHistory entity) {
    return GameHistoryDTO.builder()
        .id(entity.getId())
        .opponentType(entity.getOpponentType())
        .result(entity.getResult())
        .moveCount(entity.getMoves() != null ? entity.getMoves().size() : 0)
        .playedAt(entity.getPlayedAt())
        .build();
  }
}
