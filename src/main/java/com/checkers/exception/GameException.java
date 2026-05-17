package com.checkers.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class GameException extends RuntimeException {

  private final GameErrorCode code;
  private final HttpStatus status;

  public GameException(GameErrorCode code, HttpStatus status, String message) {
    super(message);
    this.code = code;
    this.status = status;
  }
}
