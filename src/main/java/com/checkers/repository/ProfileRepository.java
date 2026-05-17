package com.checkers.repository;

import com.checkers.entity.Profile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, String> {

  Optional<Profile> findByUsername(String username);

  boolean existsByUsername(String username);
}
