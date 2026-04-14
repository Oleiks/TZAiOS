package org.example.library.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.example.library.dto.catalog.AuthorRefDto;
import org.example.library.dto.catalog.BookAuthorRefDto;
import org.example.library.dto.catalog.BookDetailsDto;
import org.example.library.dto.catalog.BookSummaryDto;
import org.example.library.dto.catalog.BookWorkRefDto;
import org.example.library.entity.BookEntity;
import org.example.library.entity.AuthorEntity;
import org.example.library.repository.AuthorRepository;
import org.example.library.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CatalogSyncServiceTest {
  @Test
  void syncDetailsCreatesNormalizedBookAndAuthorRelations() {
    BookRepository books = mock(BookRepository.class);
    AuthorRepository authors = mock(AuthorRepository.class);
    CatalogSyncService service = new CatalogSyncService(books, authors);

    when(books.findByCatalogKey("/works/OL1W")).thenReturn(Optional.empty());
    when(authors.findByCatalogKey("/authors/OL1A")).thenReturn(Optional.empty());
    when(books.save(any(BookEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(authors.save(any(AuthorEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

    service.syncDetails(new BookDetailsDto(
        "/works/OL1W",
        "Example Book",
        "A real description.",
        "First sentence.",
        null,
        null,
        "/covers/id/12?size=M",
        List.of(12L),
        List.of(new BookAuthorRefDto(new AuthorRefDto("/authors/OL1A", "Jane Doe"))),
        List.of(new BookWorkRefDto("/works/OL1W")),
        List.of("fiction", "adventure")
    ));

    ArgumentCaptor<BookEntity> bookCaptor = ArgumentCaptor.forClass(BookEntity.class);
    ArgumentCaptor<AuthorEntity> authorCaptor = ArgumentCaptor.forClass(AuthorEntity.class);
    verify(books).save(bookCaptor.capture());
    verify(authors, atLeastOnce()).save(authorCaptor.capture());

    BookEntity saved = bookCaptor.getValue();
    assertThat(saved.getCatalogKey()).isEqualTo("/works/OL1W");
    assertThat(saved.getTitle()).isEqualTo("Example Book");
    assertThat(saved.getDescription()).isEqualTo("A real description.");
    assertThat(saved.getAuthors()).hasSize(1);
    assertThat(saved.getAuthors().iterator().next().getCatalogKey()).isEqualTo("/authors/OL1A");
    assertThat(saved.getSubjects()).containsExactlyInAnyOrder("fiction", "adventure");
  }

  @Test
  void syncSummaryPreservesAuthorNameWhenAuthorKeyMissing() {
    BookRepository books = mock(BookRepository.class);
    AuthorRepository authors = mock(AuthorRepository.class);
    CatalogSyncService service = new CatalogSyncService(books, authors);

    when(books.findByCatalogKey("/works/OL2W")).thenReturn(Optional.empty());
    when(books.save(any(BookEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

    service.syncSummary(new BookSummaryDto(
        "/works/OL2W",
        "/works/OL2W",
        "Summary Book",
        "Unknown author",
        null,
        2001,
        4.5,
        "/covers/id/44?size=M",
        List.of("history"),
        3
    ));

    ArgumentCaptor<BookEntity> bookCaptor = ArgumentCaptor.forClass(BookEntity.class);
    verify(books).save(bookCaptor.capture());

    BookEntity saved = bookCaptor.getValue();
    assertThat(saved.getCatalogKey()).isEqualTo("/works/OL2W");
    assertThat(saved.getPrimaryAuthorName()).isEqualTo("Unknown author");
  }
}
