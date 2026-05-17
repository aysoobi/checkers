package com.checkers.security;

import com.checkers.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileUserDetailsService implements UserDetailsService {

  private final ProfileRepository profileRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return profileRepository
        .findByUsername(username)
        .map(ProfileUserDetails::new)
        .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
  }
}
