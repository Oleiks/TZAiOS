package org.example.library.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.example.library.config.LibraryProperties;
import org.example.library.dto.catalog.AuthorWorksResponse;
import org.example.library.dto.catalog.BookDetailsDto;
import org.example.library.repository.AuthorRepository;
import org.example.library.repository.BookRepository;
import org.junit.jupiter.api.Test;

class CatalogServiceTest {
  @Test
  void bookNormalizesDescriptionFromAlternateFields() throws Exception {
    OpenLibraryClient client = mock(OpenLibraryClient.class);
    ApiCacheService cache = mock(ApiCacheService.class);
    CatalogSyncService syncService = mock(CatalogSyncService.class);
    BookRepository books = mock(BookRepository.class);
    AuthorRepository authors = mock(AuthorRepository.class);
    LibraryProperties properties = new LibraryProperties(
        new LibraryProperties.Security("secret", Duration.ofMinutes(30), Duration.ofDays(30)),
        new LibraryProperties.OpenLibrary("https://openlibrary.org"),
        new LibraryProperties.Cache(Duration.ofHours(6), Duration.ofHours(1), Duration.ofHours(6), Duration.ofHours(24), Duration.ofHours(24), Duration.ofHours(72)));

    CatalogService service = new CatalogService(client, cache, properties, syncService, books, authors);

    JsonNode payload = new ObjectMapper().readTree("""
        {
          "key": "/works/OL1W",
          "title": "Example Book",
          "first_sentence": { "value": "A vivid opening sentence." }
        }
        """);

    when(client.work("/works/OL1W")).thenReturn(payload);
    when(cache.cachedJson(anyString(), any(), any(), any())).thenReturn(payload);
    when(books.findByCatalogKey("/works/OL1W")).thenReturn(java.util.Optional.empty());

    BookDetailsDto result = service.book("/works/OL1W");

    assertThat(result.description()).isEqualTo("A vivid opening sentence.");
  }

  @Test
  void bookFlattensDescriptionObjectIntoText() throws Exception {
    OpenLibraryClient client = mock(OpenLibraryClient.class);
    ApiCacheService cache = mock(ApiCacheService.class);
    CatalogSyncService syncService = mock(CatalogSyncService.class);
    BookRepository books = mock(BookRepository.class);
    AuthorRepository authors = mock(AuthorRepository.class);
    LibraryProperties properties = new LibraryProperties(
        new LibraryProperties.Security("secret", Duration.ofMinutes(30), Duration.ofDays(30)),
        new LibraryProperties.OpenLibrary("https://openlibrary.org"),
        new LibraryProperties.Cache(Duration.ofHours(6), Duration.ofHours(1), Duration.ofHours(6), Duration.ofHours(24), Duration.ofHours(24), Duration.ofHours(72)));

    CatalogService service = new CatalogService(client, cache, properties, syncService, books, authors);

    JsonNode payload = new ObjectMapper().readTree("""
        {
          "key": "/works/OL1W",
          "title": "Example Book",
          "description": { "type": "/type/text", "value": "A real description from Open Library." }
        }
        """);

    when(client.work("/works/OL1W")).thenReturn(payload);
    when(cache.cachedJson(anyString(), any(), any(), any())).thenReturn(payload);
    when(books.findByCatalogKey("/works/OL1W")).thenReturn(java.util.Optional.empty());

    BookDetailsDto result = service.book("/works/OL1W");

    assertThat(result.description()).isEqualTo("A real description from Open Library.");
  }

  @Test
  void authorWorksReturnsRequestedBatchAndHydratesMissingCovers() throws Exception {
    OpenLibraryClient client = mock(OpenLibraryClient.class);
    ApiCacheService cache = mock(ApiCacheService.class);
    CatalogSyncService syncService = mock(CatalogSyncService.class);
    BookRepository books = mock(BookRepository.class);
    AuthorRepository authors = mock(AuthorRepository.class);
    LibraryProperties properties = new LibraryProperties(
        new LibraryProperties.Security("secret", Duration.ofMinutes(30), Duration.ofDays(30)),
        new LibraryProperties.OpenLibrary("https://openlibrary.org"),
        new LibraryProperties.Cache(Duration.ofHours(6), Duration.ofHours(1), Duration.ofHours(6), Duration.ofHours(24), Duration.ofHours(24), Duration.ofHours(72)));

    CatalogService service = new CatalogService(client, cache, properties, syncService, books, authors);

    JsonNode payload = new ObjectMapper().readTree("""
        {
          "entries": [
            { "key": "/works/OL1W", "title": "Work 1" },
            { "key": "/works/OL2W", "title": "Work 2", "covers": [2] },
            { "key": "/works/OL3W", "title": "Work 3", "covers": [3] },
            { "key": "/works/OL4W", "title": "Work 4", "covers": [4] },
            { "key": "/works/OL5W", "title": "Work 5", "covers": [5] },
            { "key": "/works/OL6W", "title": "Work 6", "covers": [6] },
            { "key": "/works/OL7W", "title": "Work 7", "covers": [7] },
            { "key": "/works/OL8W", "title": "Work 8", "covers": [8] },
            { "key": "/works/OL9W", "title": "Work 9", "covers": [9] },
            { "key": "/works/OL10W", "title": "Work 10", "covers": [10] },
            { "key": "/works/OL11W", "title": "Work 11", "covers": [11] }
          ]
        }
        """);

    when(client.author("/authors/OL1A")).thenReturn(new ObjectMapper().readTree("{\"key\":\"/authors/OL1A\",\"name\":\"Jane Doe\"}"));
    when(client.authorWorks("/authors/OL1A", 20)).thenReturn(payload);
    when(cache.cachedJson(anyString(), any(), any(), any())).thenAnswer(invocation -> {
      @SuppressWarnings("unchecked")
      java.util.function.Supplier<JsonNode> loader = invocation.getArgument(3);
      return loader.get();
    });
    when(books.findByAuthorKey("/authors/OL1A")).thenReturn(java.util.List.of());
    when(authors.findByCatalogKey("/authors/OL1A")).thenReturn(java.util.Optional.empty());

    AuthorWorksResponse result = service.authorWorks("/authors/OL1A", 20);

    assertThat(result.entries()).hasSize(11);
    assertThat(result.entries().get(10).title()).isEqualTo("Work 11");
  }
}
