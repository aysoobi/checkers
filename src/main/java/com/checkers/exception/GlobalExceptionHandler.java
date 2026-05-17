package com.checkers.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(GameException.class)
  public ResponseEntity<Map<String, String>> handleGame(GameException ex) {
    return ResponseEntity.status(ex.getStatus())
        .body(Map.of("code", ex.getCode().name(), "message", ex.getMessage()));
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(Map.of("code", "BAD_CREDENTIALS", "message", "Неверный логин или пароль"));
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, String>> handleStatus(ResponseStatusException ex) {
    String reason = ex.getReason() != null ? ex.getReason() : "Ошибка";
    return ResponseEntity.status(ex.getStatusCode())
        .body(Map.of("code", "ERROR", "message", reason));
  }
}
