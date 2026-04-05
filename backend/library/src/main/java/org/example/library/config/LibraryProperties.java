package org.example.library.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "library")
public record LibraryProperties(Security security, OpenLibrary openapi, Cache cache) {
  public record Security(String jwtSecret, Duration accessTokenTtl, Duration refreshTokenTtl) {}

  public record OpenLibrary(String baseUrl) {}

  public record Cache(Duration homeTtl, Duration searchTtl, Duration subjectTtl, Duration bookTtl, Duration authorTtl, Duration staleWindow) {}
}
