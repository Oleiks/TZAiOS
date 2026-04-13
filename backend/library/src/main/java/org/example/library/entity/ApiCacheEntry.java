package org.example.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "api_cache_entries")
public class ApiCacheEntry {
  public ApiCacheEntry() {}

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "cache_key", nullable = false, unique = true, length = 512)
  private String cacheKey;

  @Column(name = "payload_json", nullable = false, columnDefinition = "text")
  private String payloadJson;

  @Column(nullable = false, columnDefinition = "timestamp with time zone")
  private Instant fetchedAt;

  @Column(nullable = false, columnDefinition = "timestamp with time zone")
  private Instant expiresAt;

  @Column(nullable = false, columnDefinition = "timestamp with time zone")
  private Instant staleUntil;

}
