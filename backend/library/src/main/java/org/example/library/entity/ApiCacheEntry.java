package org.example.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "api_cache_entries")
public class ApiCacheEntry {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "cache_key", nullable = false, unique = true, length = 512)
  private String cacheKey;

  @Lob
  @Column(name = "payload_json", nullable = false)
  private String payloadJson;

  @Column(nullable = false)
  private Instant fetchedAt;

  @Column(nullable = false)
  private Instant expiresAt;

  @Column(nullable = false)
  private Instant staleUntil;
}
