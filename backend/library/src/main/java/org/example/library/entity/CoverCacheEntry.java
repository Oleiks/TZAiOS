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
@Table(name = "cover_cache_entries")
public class CoverCacheEntry {
  public CoverCacheEntry() {}

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "cache_key", nullable = false, unique = true, length = 512)
  private String cacheKey;

  @Column(name = "content_type", nullable = false, length = 100)
  private String contentType;

  @Column(name = "image_bytes", nullable = false, columnDefinition = "bytea")
  private byte[] imageBytes;

  @Column(name = "fetched_at", nullable = false, columnDefinition = "timestamp with time zone")
  private Instant fetchedAt;

}
