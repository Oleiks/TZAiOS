package org.example.library.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

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
import org.example.library.entity.AuthorEntity;
import org.example.library.entity.BookEntity;
import org.example.library.repository.AuthorRepository;
import org.example.library.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {
  private static final Logger log = LoggerFactory.getLogger(CatalogService.class);
  private static final List<String> HOME_SUBJECTS = List.of("fiction", "fantasy", "history");

  private final OpenLibraryClient client;
  private final ApiCacheService cache;
  private final LibraryProperties properties;
  private final CatalogSyncService catalogSyncService;
  private final BookRepository books;
  private final AuthorRepository authors;

  public CatalogService(
      OpenLibraryClient client,
      ApiCacheService cache,
      LibraryProperties properties,
      CatalogSyncService catalogSyncService,
      BookRepository books,
      AuthorRepository authors) {
    this.client = client;
    this.cache = cache;
    this.properties = properties;
    this.catalogSyncService = catalogSyncService;
    this.books = books;
    this.authors = authors;
  }

  @Transactional
  public HomeResponse home() {
    log.info("Building home response from subject sections");
    List<SectionDto> sections = HOME_SUBJECTS.stream()
        .map(subject -> {
          log.info("Loading home section [{}]", subject);
          String title = switch (subject) {
            case "fiction" -> "Trending Now";
            case "fantasy" -> "Fantasy Escape";
            default -> "History Picks";
          };
          return new SectionDto(subject, title, loadHomeBooksForSubject(subject, 16));
        })
        .toList();
    return new HomeResponse(sections);
  }

  @Transactional
  public SearchResponse search(String query, int page, int limit) {
    log.info("Search requested q='{}' page={}", query, page);
    int effectiveLimit = Math.min(Math.max(limit, 1), 50);
    List<BookEntity> localBooks = books.search(query);
    if (localBooks.size() < effectiveLimit) {
      JsonNode payload = cache.cachedJson(cacheKey("search", query, String.valueOf(page), String.valueOf(effectiveLimit)), properties.cache().searchTtl(), properties.cache().staleWindow(), () -> client.search(query, page, effectiveLimit));
      List<BookSummaryDto> remoteBooks = OpenLibraryMapper.fromDocs(payload.path("docs"));
      safeSync(() -> catalogSyncService.syncSummaries(remoteBooks), "search results");
      localBooks = books.search(query);
      if (!localBooks.isEmpty()) {
        return new SearchResponse(payload.path("numFound").asLong(localBooks.size()), mapSummaries(localBooks.stream().limit(effectiveLimit).toList()));
      }
      return new SearchResponse(payload.path("numFound").asLong(0), remoteBooks);
    }

    return new SearchResponse(localBooks.size(), mapSummaries(localBooks.stream().limit(effectiveLimit).toList()));
  }

  @Transactional
  public SubjectResponse subject(String subject, int limit) {
    log.info("Subject requested [{}]", subject);
    int effectiveLimit = Math.min(Math.max(limit, 1), 60);
    List<BookSummaryDto> items = loadBooksForSubject(subject, effectiveLimit);
    if (items.size() < effectiveLimit) {
      JsonNode payload = cache.cachedJson(cacheKey("subject", subject, String.valueOf(effectiveLimit)), properties.cache().subjectTtl(), properties.cache().staleWindow(), () -> client.subject(subject, effectiveLimit));
      List<BookSummaryDto> remoteBooks = new java.util.ArrayList<>();
      if (payload.path("works").isArray()) {
        for (JsonNode work : payload.path("works")) {
          if (remoteBooks.size() >= effectiveLimit) {
            break;
          }
          remoteBooks.add(OpenLibraryMapper.fromSubjectWork(work));
        }
      }
      safeSync(() -> catalogSyncService.syncSummaries(remoteBooks), "subject results");
      items = loadBooksForSubject(subject, effectiveLimit);
      if (items.isEmpty()) {
        items = remoteBooks;
      }
    }
    return new SubjectResponse(subject, items);
  }

  @Transactional
  public BookDetailsDto book(String key) {
    log.info("Book requested [{}]", key);
    BookEntity entity = books.findByCatalogKey(key).orElse(null);
    if (needsBookRefresh(entity)) {
      JsonNode payload = cache.cachedJson(cacheKey("book", key), properties.cache().bookTtl(), properties.cache().staleWindow(), () -> client.work(key));
      BookDetailsDto details = OpenLibraryMapper.fromBookDetails(payload);
      safeSync(() -> catalogSyncService.syncDetails(details), "book details");
      entity = books.findByCatalogKey(key).orElse(entity);
      if (entity == null) {
        return details;
      }
    }

    return CatalogEntityMapper.toDetails(entity);
  }

  @Transactional
  public AuthorDto author(String key) {
    log.info("Author requested [{}]", key);
    AuthorEntity entity = authors.findByCatalogKey(key).orElse(null);
    if (needsAuthorRefresh(entity)) {
      JsonNode payload = cache.cachedJson(cacheKey("author", key), properties.cache().authorTtl(), properties.cache().staleWindow(), () -> client.author(key));
      AuthorDto author = OpenLibraryMapper.fromAuthor(payload);
      safeSync(() -> catalogSyncService.syncAuthor(author), "author");
      entity = authors.findByCatalogKey(key).orElse(entity);
      if (entity == null) {
        return author;
      }
    }

    return CatalogEntityMapper.toAuthor(entity);
  }

  @Transactional
  public AuthorWorksResponse authorWorks(String key, int limit) {
    log.info("Author works requested [{}]", key);
    int effectiveLimit = Math.min(Math.max(limit, 1), 40);
    List<BookEntity> localBooks = books.findByAuthorKey(key);
    if (localBooks.size() < effectiveLimit) {
      if (needsAuthorRefresh(authors.findByCatalogKey(key).orElse(null))) {
        author(key);
      }

      JsonNode payload = cache.cachedJson(cacheKey("author-works", key, String.valueOf(effectiveLimit)), properties.cache().authorTtl(), properties.cache().staleWindow(), () -> client.authorWorks(key, effectiveLimit));
      List<BookSummaryDto> remoteWorks = new java.util.ArrayList<>();
      if (payload.path("entries").isArray()) {
        for (JsonNode work : payload.path("entries")) {
          if (remoteWorks.size() >= effectiveLimit) {
            break;
          }
          remoteWorks.add(OpenLibraryMapper.fromSubjectWork(work));
        }
      } else if (payload.isArray()) {
        for (JsonNode work : payload) {
          if (remoteWorks.size() >= effectiveLimit) {
            break;
          }
          remoteWorks.add(OpenLibraryMapper.fromSubjectWork(work));
        }
      }
      safeSync(() -> catalogSyncService.syncAuthorWorks(key, resolveAuthorName(key), remoteWorks), "author works");
      localBooks = books.findByAuthorKey(key);
      hydrateMissingBookDetails(localBooks.stream().limit(effectiveLimit).toList());
      localBooks = books.findByAuthorKey(key);
      if (localBooks.isEmpty()) {
        return new AuthorWorksResponse(remoteWorks);
      }
    }

    hydrateMissingBookDetails(localBooks.stream().limit(effectiveLimit).toList());
    localBooks = books.findByAuthorKey(key);
    return new AuthorWorksResponse(mapSummaries(localBooks.stream().limit(effectiveLimit).toList()));
  }

  private String cacheKey(String prefix, String... parts) {
    StringBuilder builder = new StringBuilder(prefix);
    for (String part : parts) {
      builder.append(':').append(part == null ? "" : part.trim().toLowerCase());
    }
    return builder.toString();
  }

  private void safeSync(Runnable sync, String label) {
    try {
      sync.run();
    } catch (Exception ex) {
      log.warn("Catalog sync failed for {}", label, ex);
    }
  }

  private List<BookSummaryDto> loadBooksForSubject(String subject, int limit) {
    List<BookEntity> localBooks = books.findBySubject(subject);
    if (localBooks.isEmpty()) {
      return List.of();
    }
    return mapSummaries(localBooks.stream().limit(limit).toList());
  }

  private List<BookSummaryDto> loadHomeBooksForSubject(String subject, int limit) {
    List<BookSummaryDto> items = loadBooksForSubject(subject, limit);
    if (items.size() >= limit) {
      return items;
    }

    JsonNode payload = cache.cachedJson(cacheKey("subject", subject, String.valueOf(limit)), properties.cache().subjectTtl(), properties.cache().staleWindow(), () -> client.subject(subject, limit));
    List<BookSummaryDto> remoteBooks = new java.util.ArrayList<>();
    if (payload.path("works").isArray()) {
      for (JsonNode work : payload.path("works")) {
        if (remoteBooks.size() >= limit) {
          break;
        }
        remoteBooks.add(OpenLibraryMapper.fromSubjectWork(work));
      }
    }
    safeSync(() -> catalogSyncService.syncSummaries(remoteBooks), "home subject results");
    items = loadBooksForSubject(subject, limit);
    return items.isEmpty() ? remoteBooks : items;
  }

  private List<BookSummaryDto> mapSummaries(List<BookEntity> localBooks) {
    return localBooks.stream().map(CatalogEntityMapper::toSummary).toList();
  }

  private boolean needsBookRefresh(BookEntity entity) {
    return entity == null
        || isBlank(entity.getTitle())
        || isBlank(entity.getDescription())
        || isBlank(entity.getCoverUrl())
        || entity.getAuthors().isEmpty();
  }

  private boolean needsAuthorRefresh(AuthorEntity entity) {
    return entity == null || isBlank(entity.getName()) || isBlank(entity.getBio());
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private void hydrateMissingBookDetails(List<BookEntity> localBooks) {
    for (BookEntity book : localBooks) {
      if (book == null || !needsBookRefresh(book)) {
        continue;
      }

      JsonNode payload = cache.cachedJson(cacheKey("book", book.getCatalogKey()), properties.cache().bookTtl(), properties.cache().staleWindow(), () -> client.work(book.getCatalogKey()));
      BookDetailsDto details = OpenLibraryMapper.fromBookDetails(payload);
      safeSync(() -> catalogSyncService.syncDetails(details), "book details");
    }
  }

  private String resolveAuthorName(String key) {
    return authors.findByCatalogKey(key).map(author -> author.getName()).orElse(null);
  }

}
