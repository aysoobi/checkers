package com.checkers.config;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Railway injects DATABASE_URL (postgresql://user:pass@host:port/db).
 * PG* variables are used when DATABASE_URL is absent.
 */
@Configuration
@Profile("prod")
public class RailwayDatabaseConfig {

  @Bean
  @Primary
  @ConditionalOnProperty(name = "DATABASE_URL")
  public DataSource railwayDataSource(@Value("${DATABASE_URL}") String databaseUrl) {
    ParsedUrl parsed = ParsedUrl.from(databaseUrl);
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl(parsed.jdbcUrl());
    dataSource.setUsername(parsed.username());
    dataSource.setPassword(parsed.password());
    return dataSource;
  }

  private record ParsedUrl(String jdbcUrl, String username, String password) {

    static ParsedUrl from(String databaseUrl) {
      String normalized = databaseUrl.replaceFirst("^postgres://", "postgresql://");
      if (!normalized.startsWith("postgresql://")) {
        throw new IllegalArgumentException("Unsupported DATABASE_URL format");
      }
      String withoutScheme = normalized.substring("postgresql://".length());
      int at = withoutScheme.lastIndexOf('@');
      if (at < 0) {
        throw new IllegalArgumentException("DATABASE_URL must contain credentials");
      }
      String userInfo = withoutScheme.substring(0, at);
      String hostAndDb = withoutScheme.substring(at + 1);
      String[] credentials = userInfo.split(":", 2);
      String username = credentials[0];
      String password = credentials.length > 1 ? credentials[1] : "";
      return new ParsedUrl("jdbc:postgresql://" + hostAndDb, username, password);
    }
  }
}
