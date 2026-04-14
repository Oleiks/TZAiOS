package org.example.library.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class OpenLibraryClient {
  private static final Logger log = LoggerFactory.getLogger(OpenLibraryClient.class);

  private final RestClient restClient;
  private final ObjectMapper objectMapper;

  public JsonNode search(String query, int page, int limit) {
    return getJson("/search.json", Map.of("q", query, "page", page, "limit", normalizeLimit(limit, 20, 50)));
  }

  public JsonNode subject(String subject, int limit) {
    return getJson("/subjects/" + subject + ".json", Map.of("limit", normalizeLimit(limit, 20, 60)));
  }

  public JsonNode work(String key) {
    String current = normalizeKey(key);
    JsonNode editionPayload = null;
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
        editionPayload = node;
        String workKey = node.get("works").get(0).path("key").asText(null);
        if (workKey != null && !workKey.isBlank()) {
          String next = normalizeKey(workKey);
          if (visited.contains(next)) {
            log.warn("Detected OpenLibrary work redirect cycle {} -> {}", current, next);
            return mergeWorkPayload(node, editionPayload);
          }
          current = next;
          continue;
        }
      }

      JsonNode merged = mergeWorkPayload(node, editionPayload);

      return merged;
    }

    JsonNode merged = mergeWorkPayload(getJson(current + ".json", Map.of()), editionPayload);
    return merged;
  }

  public JsonNode author(String key) {
    return getJson(normalizeKey(key) + ".json", Map.of());
  }

  public JsonNode authorWorks(String key, int limit) {
    return getJson(normalizeKey(key) + "/works.json", Map.of("limit", normalizeLimit(limit, 20, 40)));
  }

  private JsonNode getJson(String path, Map<String, ?> query) {
    if (log.isInfoEnabled()) {
      log.info("OpenLibrary GET {} query={}", path, query);
    }
    try {
      String body = restClient.get()
          .uri(uriBuilder -> {
            var builder = uriBuilder.path(path);
            query.forEach((name, value) -> builder.queryParam(name, value));
            return builder.build();
          })
          .retrieve()
          .body(String.class);

      if (body == null || body.isBlank()) {
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenLibrary returned an empty response for " + path);
      }

      return objectMapper.readTree(body);
    } catch (RestClientResponseException ex) {
      log.warn("OpenLibrary request failed path={} status={} body={}", path, ex.getStatusCode(), excerpt(ex.getResponseBodyAsString()));
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenLibrary request failed for " + path, ex);
    } catch (Exception ex) {
      log.warn("OpenLibrary response parse failed path={} error={}", path, ex.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenLibrary returned invalid JSON for " + path, ex);
    }
  }

  private String excerpt(String body) {
    if (body == null) {
      return "<null>";
    }
    String normalized = body.replaceAll("\\s+", " ").trim();
    return normalized.length() <= 200 ? normalized : normalized.substring(0, 200) + "...";
  }

  private String normalizeKey(String key) {
    if (key == null || key.isBlank()) {
      throw new IllegalArgumentException("Key is required");
    }
    return key.startsWith("/") ? key : "/" + key;
  }

  private int normalizeLimit(int requested, int defaultValue, int maxValue) {
    int value = requested > 0 ? requested : defaultValue;
    return Math.min(value, maxValue);
  }

  static JsonNode mergeWorkPayload(JsonNode workPayload, JsonNode editionPayload) {
    if (workPayload == null || !workPayload.isObject() || editionPayload == null || !editionPayload.isObject()) {
      return workPayload;
    }

    ObjectNode merged = ((ObjectNode) workPayload).deepCopy();
    copyIfMissing(merged, editionPayload, "description");
    copyIfMissing(merged, editionPayload, "first_sentence");
    copyIfMissing(merged, editionPayload, "notes");
    copyIfMissing(merged, editionPayload, "excerpt");
    copyIfMissing(merged, editionPayload, "excerpts");
    copyIfMissing(merged, editionPayload, "cover_i");
    copyIfMissing(merged, editionPayload, "cover_id");
    copyIfMissing(merged, editionPayload, "cover_edition_key");
    copyIfMissing(merged, editionPayload, "covers");
    copyIfMissing(merged, editionPayload, "isbn");
    return merged;
  }

  private static void copyIfMissing(ObjectNode target, JsonNode source, String field) {
    JsonNode current = target.get(field);
    if (current != null && !current.isNull() && (!current.isTextual() || !current.asText("").trim().isEmpty())) {
      return;
    }

    JsonNode value = source.get(field);
    if (value != null && !value.isNull()) {
      target.set(field, value);
    }
  }
}
