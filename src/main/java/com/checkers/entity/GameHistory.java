package com.checkers.entity;

import com.checkers.converter.JacksonConverter;
import com.checkers.model.Move;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "games")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameHistory {

  @Id
  @Column(length = 36, nullable = false)
  private String id;

  @Column(name = "user_id", nullable = false, length = 36)
  private String userId;

  @Column(name = "opponent_type", nullable = false, length = 16)
  private String opponentType;

  @Column(length = 8)
  private String result;

  @Convert(converter = JacksonConverter.class)
  @Column(length = 100000)
  private List<Move> moves;

  @CreationTimestamp
  @Column(name = "played_at", nullable = false, updatable = false)
  private OffsetDateTime playedAt;
}
