package checkers.incubator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.test.context.TestConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@Import(IncubatorApplicationTests.TestSecurity.class)
class IncubatorApplicationTests {

  @Test
  void contextLoads() {}

  @TestConfiguration
  static class TestSecurity {
    @Bean
    @Primary
    SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
      return http.csrf(csrf -> csrf.disable())
          .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
          .build();
    }
  }
}
