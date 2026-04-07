package org.example.library.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.library.config.LibraryProperties;
import org.example.library.dto.catalog.HomeResponse;
import org.example.library.dto.catalog.SearchResponse;
import org.example.library.dto.catalog.SectionDto;
import org.example.library.dto.catalog.SubjectResponse;
import org.springframework.stereotype.Service;

@Service
public class CatalogService {
  private static final Logger log = LoggerFactory.getLogger(CatalogService.class);
  private static final List<String> HOME_SUBJECTS = List.of("fiction", "fantasy", "history");

  private final OpenLibraryClient client;
  private final ApiCacheService cache;
  private final LibraryProperties properties;
  private final ObjectMapper objectMapper;

  public CatalogService(OpenLibraryClient client, ApiCacheService cache, LibraryProperties properties, ObjectMapper objectMapper) {
    this.client = client;
    this.cache = cache;
    this.properties = properties;
    this.objectMapper = objectMapper;
  }

  public HomeResponse home() {
    log.info("Building home response from subject sections");
    List<SectionDto> sections = HOME_SUBJECTS.stream()
        .map(subject -> {
          log.info("Loading home section [{}]", subject);
          SubjectResponse response = subject(subject);
          String title = switch (subject) {
            case "fiction" -> "Trending Now";
            case "fantasy" -> "Fantasy Escape";
            default -> "History Picks";
          };
          return new SectionDto(subject, title, response.works());
        })
        .toList();
    return new HomeResponse(sections);
  }

  public SearchResponse search(String query, int page) {
    log.info("Search requested q='{}' page={}", query, page);
    JsonNode payload = cache.cachedJson(cacheKey("search", query, String.valueOf(page)), properties.cache().searchTtl(), properties.cache().staleWindow(), () -> client.search(query, page));
    return new SearchResponse(payload.path("numFound").asLong(0), OpenLibraryMapper.fromDocs(payload.path("docs")));
  }

  public SubjectResponse subject(String subject) {
    log.info("Subject requested [{}]", subject);
    JsonNode payload = cache.cachedJson(cacheKey("subject", subject), properties.cache().subjectTtl(), properties.cache().staleWindow(), () -> client.subject(subject));
    List<org.example.library.dto.catalog.BookSummaryDto> books = new java.util.ArrayList<>();
    if (payload.path("works").isArray()) {
      for (JsonNode work : payload.path("works")) {
        books.add(OpenLibraryMapper.fromSubjectWork(work));
      }
    }
    return new SubjectResponse(payload.path("name").asText(subject), books);
  }

  public JsonNode book(String key) {
    log.info("Book requested [{}]", key);
    return cache.cachedJson(cacheKey("book", key), properties.cache().bookTtl(), properties.cache().staleWindow(), () -> client.work(key));
  }

  public JsonNode editions(String key) {
    log.info("Book editions requested [{}]", key);
    return cache.cachedJson(cacheKey("editions", key), properties.cache().bookTtl(), properties.cache().staleWindow(), () -> client.editions(key));
  }

  public JsonNode author(String key) {
    log.info("Author requested [{}]", key);
    return cache.cachedJson(cacheKey("author", key), properties.cache().authorTtl(), properties.cache().staleWindow(), () -> client.author(key));
  }

  public JsonNode authorWorks(String key) {
    log.info("Author works requested [{}]", key);
    return cache.cachedJson(cacheKey("author-works", key), properties.cache().authorTtl(), properties.cache().staleWindow(), () -> client.authorWorks(key));
  }

  private String cacheKey(String prefix, String... parts) {
    StringBuilder builder = new StringBuilder(prefix);
    for (String part : parts) {
      builder.append(':').append(part == null ? "" : part.trim().toLowerCase());
    }
    return builder.toString();
  }
}
