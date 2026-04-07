package org.example.library.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.example.library.entity.ApiCacheEntry;
import org.example.library.repository.ApiCacheRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApiCacheService {
  private final ApiCacheRepository repository;
  private final ObjectMapper objectMapper;

  public ApiCacheService(ApiCacheRepository repository, ObjectMapper objectMapper) {
    this.repository = repository;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public JsonNode cachedJson(String cacheKey, Duration ttl, Duration staleWindow, Supplier<JsonNode> loader) {
    Instant now = Instant.now();
    var existing = repository.findByCacheKey(cacheKey);
    if (existing.isPresent()) {
      ApiCacheEntry entry = existing.get();
      if (now.isBefore(entry.getExpiresAt())) {
        return read(entry.getPayloadJson());
      }
      if (now.isBefore(entry.getStaleUntil())) {
        refreshAsync(cacheKey, ttl, staleWindow, loader);
        return read(entry.getPayloadJson());
      }
    }
    JsonNode fresh = loader.get();
    save(cacheKey, fresh, ttl, staleWindow);
    return fresh;
  }

  private void refreshAsync(String cacheKey, Duration ttl, Duration staleWindow, Supplier<JsonNode> loader) {
    CompletableFuture.runAsync(() -> {
      try {
        JsonNode fresh = loader.get();
        save(cacheKey, fresh, ttl, staleWindow);
      } catch (Exception ignored) {
      }
    });
  }

  @Transactional
  public void save(String cacheKey, JsonNode payload, Duration ttl, Duration staleWindow) {
    Instant now = Instant.now();
    ApiCacheEntry entry = repository.findByCacheKey(cacheKey).orElseGet(ApiCacheEntry::new);
    entry.setCacheKey(cacheKey);
    entry.setPayloadJson(payload.toString());
    entry.setFetchedAt(now);
    entry.setExpiresAt(now.plus(ttl));
    entry.setStaleUntil(now.plus(ttl).plus(staleWindow));
    repository.save(entry);
  }

  private JsonNode read(String payloadJson) {
    try {
      return objectMapper.readTree(payloadJson);
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to read cached payload", ex);
    }
  }
}
