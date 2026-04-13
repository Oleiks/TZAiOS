package org.example.library.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

import lombok.RequiredArgsConstructor;
import org.example.library.entity.CoverCacheEntry;
import org.example.library.repository.CoverCacheRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CoverProxyService {
  private static final Logger log = LoggerFactory.getLogger(CoverProxyService.class);
  private static final URI BASE_URI = URI.create("https://covers.openlibrary.org");

  private final CoverCacheRepository cacheRepository;
  private final HttpClient httpClient = HttpClient.newBuilder()
      .followRedirects(HttpClient.Redirect.ALWAYS)
      .build();

  @Transactional
  public CoverImage fetchById(String coverId, String size) {
    String normalizedId = normalizePositiveIdentifier(coverId);
    String normalizedSize = normalizeSize(size);
    return fetch("id", normalizedId, normalizedSize);
  }

  @Transactional
  public CoverImage fetchByOlid(String olid, String size) {
    String normalizedOlid = normalizeTextIdentifier(olid);
    String normalizedSize = normalizeSize(size);
    return fetch("olid", normalizedOlid, normalizedSize);
  }

  @Transactional
  public CoverImage fetchByIsbn(String isbn, String size) {
    String normalizedIsbn = normalizeTextIdentifier(isbn);
    String normalizedSize = normalizeSize(size);
    return fetch("isbn", normalizedIsbn, normalizedSize);
  }

  private CoverImage fetch(String type, String identifier, String size) {
    String cacheKey = type + ":" + identifier + ":" + size;
    return cacheRepository.findByCacheKey(cacheKey)
        .map(entry -> {
          log.info("Cover cache hit [{}]", cacheKey);
          return toCoverImage(entry);
        })
        .orElseGet(() -> {
          log.info("Cover cache miss [{}]", cacheKey);
          return fetchAndCache(type, identifier, size, cacheKey);
        });
  }

  private CoverImage fetchAndCache(String type, String identifier, String size, String cacheKey) {
    String path = "/b/" + type + "/" + identifier + "-" + size + ".jpg";
    log.info("Proxy cover fetch [{}]", path);
    try {
      HttpResponse<byte[]> response = httpClient.send(
          HttpRequest.newBuilder(BASE_URI.resolve(path))
              .GET()
              .header("Accept", "image/*")
              .header("User-Agent", "idzi-library-backend (openlibrary-proxy)")
              .timeout(Duration.ofSeconds(10))
              .build(),
          HttpResponse.BodyHandlers.ofByteArray());

      if (response.statusCode() != 200 || response.body() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to fetch cover image");
      }

      String contentType = response.headers().firstValue("content-type").orElse(MediaType.IMAGE_JPEG_VALUE);
      save(cacheKey, contentType, response.body());
      log.info("Cover fetch completed [{}] status={}", path, response.statusCode());
      return new CoverImage(response.body(), MediaType.parseMediaType(contentType));
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cover request interrupted", ex);
    } catch (IOException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to fetch cover image", ex);
    }
  }

  private void save(String cacheKey, String contentType, byte[] imageBytes) {
    CoverCacheEntry entry = cacheRepository.findByCacheKey(cacheKey).orElseGet(CoverCacheEntry::new);
    entry.setCacheKey(cacheKey);
    entry.setContentType(contentType);
    entry.setImageBytes(imageBytes);
    entry.setFetchedAt(Instant.now());
    cacheRepository.save(entry);
    log.info("Cover cache stored [{}] bytes={}", cacheKey, imageBytes.length);
  }

  private CoverImage toCoverImage(CoverCacheEntry entry) {
    return new CoverImage(entry.getImageBytes(), MediaType.parseMediaType(entry.getContentType()));
  }

  private String normalizeSize(String size) {
    String value = size == null ? "M" : size.trim().toUpperCase(Locale.ROOT);
    return switch (value) {
      case "S", "M", "L" -> value;
      default -> "M";
    };
  }

  private String normalizePositiveIdentifier(String identifier) {
    String normalized = normalizeTextIdentifier(identifier);
    try {
      long value = Long.parseLong(normalized);
      if (value <= 0) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cover identifier must be positive");
      }
      return Long.toString(value);
    } catch (NumberFormatException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cover identifier must be numeric", ex);
    }
  }

  private String normalizeTextIdentifier(String identifier) {
    String normalized = identifier == null ? "" : identifier.trim();
    if (normalized.isEmpty() || "-1".equals(normalized)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cover identifier is invalid");
    }
    return normalized;
  }

  public record CoverImage(byte[] bytes, MediaType contentType) {}
}
