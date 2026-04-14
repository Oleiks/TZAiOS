package org.example.library.service;

import java.util.LinkedHashSet;
import java.util.List;
import org.example.library.dto.catalog.AuthorDto;
import org.example.library.dto.catalog.BookAuthorRefDto;
import org.example.library.dto.catalog.BookDetailsDto;
import org.example.library.dto.catalog.BookSummaryDto;
import org.example.library.entity.AuthorEntity;
import org.example.library.entity.BookEntity;
import org.example.library.repository.AuthorRepository;
import org.example.library.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogSyncService {
  private final BookRepository books;
  private final AuthorRepository authors;

  public CatalogSyncService(BookRepository books, AuthorRepository authors) {
    this.books = books;
    this.authors = authors;
  }

  @Transactional
  public void syncSummaries(List<BookSummaryDto> items) {
    if (items == null || items.isEmpty()) {
      return;
    }

    for (BookSummaryDto item : items) {
      syncSummary(item);
    }
  }

  @Transactional
  public void syncSummary(BookSummaryDto item) {
    if (item == null) {
      return;
    }

    BookEntity book = findOrCreateBook(catalogKey(item.key(), item.workKey()));
    book.setTitle(resolveText(item.title(), book.getTitle(), "Untitled"));
    book.setPrimaryAuthorName(resolveText(item.authorName(), book.getPrimaryAuthorName(), null));
    book.setFirstPublishYear(item.firstPublishYear() != null ? item.firstPublishYear() : book.getFirstPublishYear());
    book.setRating(item.rating() != null ? item.rating() : book.getRating());
    book.setCoverUrl(resolveText(item.coverUrl(), book.getCoverUrl(), null));
    book.setEditionCount(Math.max(item.editionCount(), book.getEditionCount()));

    replaceSubjects(book, item.subjects());
    replacePrimaryAuthor(book, item.authorKey(), item.authorName());

    books.save(book);
  }

  @Transactional
  public void syncDetails(BookDetailsDto details) {
    if (details == null || details.key() == null || details.key().isBlank()) {
      return;
    }

    BookEntity book = findOrCreateBook(details.key());
    book.setTitle(resolveText(details.title(), book.getTitle(), "Untitled"));
    book.setDescription(resolveText(details.description(), book.getDescription(), null));
    book.setFirstSentence(resolveText(details.firstSentence(), book.getFirstSentence(), null));
    book.setNotes(resolveText(details.notes(), book.getNotes(), null));
    book.setExcerpt(resolveText(details.excerpt(), book.getExcerpt(), null));
    book.setCoverUrl(resolveText(details.coverUrl(), book.getCoverUrl(), null));

    replaceSubjects(book, details.subjects());
    replaceWorkKeys(book, details.works());
    replaceAuthors(book, details.authors());

    books.save(book);
  }

  @Transactional
  public void syncAuthor(AuthorDto author) {
    if (author == null || author.key() == null || author.key().isBlank()) {
      return;
    }

    AuthorEntity entity = findOrCreateAuthor(author.key());
    entity.setName(resolveText(author.name(), entity.getName(), "Unknown author"));
    entity.setBio(resolveText(author.bio(), entity.getBio(), null));
    entity.setPersonalName(resolveText(author.personalName(), entity.getPersonalName(), null));

    if (author.photos() != null && !author.photos().isEmpty()) {
      entity.setPhotos(new LinkedHashSet<>(author.photos()));
    }

    authors.save(entity);
  }

  @Transactional
  public void syncAuthorWorks(String authorKey, String authorName, List<BookSummaryDto> items) {
    if (items == null || items.isEmpty()) {
      return;
    }

    for (BookSummaryDto item : items) {
      if (item == null) {
        continue;
      }

      BookSummaryDto normalized = new BookSummaryDto(
          item.key(),
          item.workKey(),
          item.title(),
          resolveText(item.authorName(), authorName, "Unknown author"),
          resolveText(item.authorKey(), authorKey, null),
          item.firstPublishYear(),
          item.rating(),
          item.coverUrl(),
          item.subjects(),
          item.editionCount());
      syncSummary(normalized);
    }
  }

  private BookEntity findOrCreateBook(String catalogKey) {
    String normalizedKey = normalizeKey(catalogKey);
    return books.findByCatalogKey(normalizedKey).orElseGet(() -> {
      BookEntity entity = new BookEntity();
      entity.setCatalogKey(normalizedKey);
      entity.setTitle("Untitled");
      entity.setEditionCount(0);
      return entity;
    });
  }

  private AuthorEntity findOrCreateAuthor(String catalogKey) {
    String normalizedKey = normalizeKey(catalogKey);
    return authors.findByCatalogKey(normalizedKey).orElseGet(() -> {
      AuthorEntity entity = new AuthorEntity();
      entity.setCatalogKey(normalizedKey);
      entity.setName("Unknown author");
      return entity;
    });
  }

  private void replacePrimaryAuthor(BookEntity book, String authorKey, String authorName) {
    if (authorKey == null || authorKey.isBlank()) {
      if (book.getPrimaryAuthorName() == null || book.getPrimaryAuthorName().isBlank()) {
        book.setPrimaryAuthorName(authorName);
      }
      return;
    }

    AuthorEntity author = findOrCreateAuthor(authorKey);
    author.setName(resolveText(authorName, author.getName(), "Unknown author"));
    authors.save(author);

    book.getAuthors().add(author);
    if (book.getPrimaryAuthorName() == null || book.getPrimaryAuthorName().isBlank()) {
      book.setPrimaryAuthorName(author.getName());
    }
  }

  private void replaceAuthors(BookEntity book, List<BookAuthorRefDto> refs) {
    if (refs == null || refs.isEmpty()) {
      return;
    }

    book.getAuthors().clear();
    for (BookAuthorRefDto ref : refs) {
      if (ref == null || ref.author() == null || ref.author().key() == null || ref.author().key().isBlank()) {
        continue;
      }

      AuthorEntity author = findOrCreateAuthor(ref.author().key());
      author.setName(resolveText(ref.author().name(), author.getName(), "Unknown author"));
      authors.save(author);
      book.getAuthors().add(author);
    }

    if (!book.getAuthors().isEmpty()) {
      book.setPrimaryAuthorName(book.getAuthors().iterator().next().getName());
    }
  }

  private void replaceSubjects(BookEntity book, List<String> subjects) {
    if (subjects == null || subjects.isEmpty()) {
      return;
    }

    book.getSubjects().clear();
    for (String subject : subjects) {
      if (subject != null && !subject.isBlank()) {
        book.getSubjects().add(subject.trim());
      }
    }
  }

  private void replaceWorkKeys(BookEntity book, List<org.example.library.dto.catalog.BookWorkRefDto> works) {
    if (works == null || works.isEmpty()) {
      return;
    }

    book.getWorkKeys().clear();
    for (org.example.library.dto.catalog.BookWorkRefDto work : works) {
      if (work != null && work.key() != null && !work.key().isBlank()) {
        book.getWorkKeys().add(work.key().trim());
      }
    }
  }

  private String catalogKey(String key, String workKey) {
    return normalizeKey(workKey != null && !workKey.isBlank() ? workKey : key);
  }

  private String normalizeKey(String key) {
    if (key == null || key.isBlank()) {
      throw new IllegalArgumentException("Catalog key is required");
    }
    return key.trim();
  }

  private String resolveText(String incoming, String existing, String fallback) {
    if (incoming != null && !incoming.isBlank()) {
      return incoming.trim();
    }
    if (existing != null && !existing.isBlank()) {
      return existing;
    }
    return fallback;
  }
}
