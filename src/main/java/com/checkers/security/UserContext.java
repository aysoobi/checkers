package com.checkers.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class UserContext {

  private UserContext() {}

  public static String getUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof ProfileUserDetails details) {
      return details.getUserId();
    }
    return null;
  }

  public static String requireUserId() {
    String userId = getUserId();
    if (userId == null || userId.isBlank()) {
      throw new IllegalStateException("Пользователь не авторизован");
    }
    return userId;
  }
}
