package org.example.library.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.library.config.LibraryProperties;
import org.example.library.dto.catalog.AuthorDto;
import org.example.library.dto.catalog.AuthorWorksResponse;
import org.example.library.dto.catalog.BookDetailsDto;
import org.example.library.dto.catalog.HomeResponse;
import org.example.library.dto.catalog.BookSummaryDto;
import org.example.library.dto.catalog.SearchResponse;
import org.example.library.dto.catalog.SectionDto;
import org.example.library.dto.catalog.SubjectResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CatalogService {
  private static final Logger log = LoggerFactory.getLogger(CatalogService.class);
  private static final List<String> HOME_SUBJECTS = List.of("fiction", "fantasy", "history");

  private final OpenLibraryClient client;
  private final ApiCacheService cache;
  private final LibraryProperties properties;

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

  public BookDetailsDto book(String key) {
    log.info("Book requested [{}]", key);
    JsonNode payload = cache.cachedJson(cacheKey("book", key), properties.cache().bookTtl(), properties.cache().staleWindow(), () -> client.work(key));
    return OpenLibraryMapper.fromBookDetails(payload);
  }

  public AuthorDto author(String key) {
    log.info("Author requested [{}]", key);
    JsonNode payload = cache.cachedJson(cacheKey("author", key), properties.cache().authorTtl(), properties.cache().staleWindow(), () -> client.author(key));
    return OpenLibraryMapper.fromAuthor(payload);
  }

  public AuthorWorksResponse authorWorks(String key, int limit) {
    log.info("Author works requested [{}]", key);
    JsonNode payload = cache.cachedJson(cacheKey("author-works", key), properties.cache().authorTtl(), properties.cache().staleWindow(), () -> client.authorWorks(key));
    int effectiveLimit = Math.min(Math.max(limit, 1), 10);
    List<BookSummaryDto> works = new java.util.ArrayList<>();
    if (payload.path("entries").isArray()) {
      for (JsonNode work : payload.path("entries")) {
        if (works.size() >= effectiveLimit) {
          break;
        }
        works.add(enrichAuthorWork(OpenLibraryMapper.fromSubjectWork(work)));
      }
    } else if (payload.isArray()) {
      for (JsonNode work : payload) {
        if (works.size() >= effectiveLimit) {
          break;
        }
        works.add(enrichAuthorWork(OpenLibraryMapper.fromSubjectWork(work)));
      }
    }
    return new AuthorWorksResponse(works);
  }

  private String cacheKey(String prefix, String... parts) {
    StringBuilder builder = new StringBuilder(prefix);
    for (String part : parts) {
      builder.append(':').append(part == null ? "" : part.trim().toLowerCase());
    }
    return builder.toString();
  }

  private BookSummaryDto enrichAuthorWork(BookSummaryDto summary) {
    if (summary.coverUrl() != null || summary.workKey() == null || summary.workKey().isBlank()) {
      return summary;
    }

    JsonNode detail = cache.cachedJson(
        cacheKey("book", summary.workKey()),
        properties.cache().bookTtl(),
        properties.cache().staleWindow(),
        () -> client.work(summary.workKey())
    );
    String coverUrl = OpenLibraryMapper.fromBookDetails(detail).coverUrl();
    if (coverUrl == null) {
      return summary;
    }

    return new BookSummaryDto(
        summary.key(),
        summary.workKey(),
        summary.title(),
        summary.authorName(),
        summary.authorKey(),
        summary.firstPublishYear(),
        summary.rating(),
        coverUrl,
        summary.subjects(),
        summary.editionCount());
  }

}
