package org.example.library.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenLibraryClient {
  private static final Logger log = LoggerFactory.getLogger(OpenLibraryClient.class);

  private final RestClient restClient;

  public OpenLibraryClient(@Qualifier("openLibraryRestClient") RestClient openLibraryRestClient) {
    this.restClient = openLibraryRestClient;
  }

  public JsonNode search(String query, int page) {
    return getJson("/search.json", Map.of("q", query, "page", page, "limit", 20));
  }

  public JsonNode subject(String subject) {
    return getJson("/subjects/" + subject + ".json", Map.of("limit", 20));
  }

  public JsonNode work(String key) {
    String current = normalizeKey(key);
    java.util.Set<String> visited = new java.util.HashSet<>();

    for (int i = 0; i < 5; i++) {
      if (!visited.add(current)) {
        log.warn("Detected OpenLibrary work redirect loop for {}", current);
        break;
      }

      JsonNode node = getJson(current + ".json", Map.of());
      if (node.hasNonNull("location")) {
        current = normalizeKey(node.get("location").asText());
        continue;
      }

      if (current.startsWith("/books/") && node.has("works") && node.get("works").isArray() && !node.get("works").isEmpty()) {
        String workKey = node.get("works").get(0).path("key").asText(null);
        if (workKey != null && !workKey.isBlank()) {
          String next = normalizeKey(workKey);
          if (visited.contains(next)) {
            log.warn("Detected OpenLibrary work redirect cycle {} -> {}", current, next);
            return node;
          }
          current = next;
          continue;
        }
      }

      return node;
    }

    return getJson(current + ".json", Map.of());
  }

  public JsonNode editions(String key) {
    String normalized = normalizeKey(key);
    if (normalized.startsWith("/books/")) {
      return getJson(normalized + ".json", Map.of());
    }
    return getJson(normalized + "/editions.json", Map.of("limit", 20));
  }

  public JsonNode author(String key) {
    return getJson(normalizeKey(key) + ".json", Map.of());
  }

  public JsonNode authorWorks(String key) {
    return getJson(normalizeKey(key) + "/works.json", Map.of("limit", 20));
  }

  private JsonNode getJson(String path, Map<String, ?> query) {
    if (log.isInfoEnabled()) {
      log.info("OpenLibrary GET {} query={}", path, query);
    }
    return restClient.get()
        .uri(uriBuilder -> {
          var builder = uriBuilder.path(path);
          query.forEach((name, value) -> builder.queryParam(name, value));
          return builder.build();
        })
        .retrieve()
        .body(JsonNode.class);
  }

  private String normalizeKey(String key) {
    if (key == null || key.isBlank()) {
      throw new IllegalArgumentException("Key is required");
    }
    return key.startsWith("/") ? key : "/" + key;
  }
}
