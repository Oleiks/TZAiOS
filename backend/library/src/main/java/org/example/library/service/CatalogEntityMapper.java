package org.example.library.service;

import java.util.ArrayList;
import java.util.List;
import org.example.library.dto.catalog.AuthorDto;
import org.example.library.dto.catalog.AuthorRefDto;
import org.example.library.dto.catalog.AuthorWorksResponse;
import org.example.library.dto.catalog.BookAuthorRefDto;
import org.example.library.dto.catalog.BookDetailsDto;
import org.example.library.dto.catalog.BookSummaryDto;
import org.example.library.dto.catalog.BookWorkRefDto;
import org.example.library.entity.AuthorEntity;
import org.example.library.entity.BookEntity;

final class CatalogEntityMapper {
  private CatalogEntityMapper() {}

  static BookSummaryDto toSummary(BookEntity book) {
    AuthorEntity primaryAuthor = book.getAuthors().stream().findFirst().orElse(null);
    String authorName = book.getPrimaryAuthorName();
    if (authorName == null || authorName.isBlank()) {
      authorName = primaryAuthor != null ? primaryAuthor.getName() : "Unknown author";
    }

    String authorKey = primaryAuthor != null ? primaryAuthor.getCatalogKey() : null;
    return new BookSummaryDto(
        book.getCatalogKey(),
        book.getCatalogKey(),
        book.getTitle(),
        authorName,
        authorKey,
        book.getFirstPublishYear(),
        book.getRating(),
        book.getCoverUrl(),
        new ArrayList<>(book.getSubjects()),
        book.getEditionCount());
  }

  static BookDetailsDto toDetails(BookEntity book) {
    List<BookAuthorRefDto> authors = book.getAuthors().stream()
        .map(author -> new BookAuthorRefDto(new AuthorRefDto(author.getCatalogKey(), author.getName())))
        .toList();
    List<BookWorkRefDto> works = book.getWorkKeys().stream().map(BookWorkRefDto::new).toList();
    return new BookDetailsDto(
        book.getCatalogKey(),
        book.getTitle(),
        firstText(book.getDescription(), book.getFirstSentence(), book.getNotes(), book.getExcerpt()),
        book.getFirstSentence(),
        book.getNotes(),
        book.getExcerpt(),
        book.getCoverUrl(),
        List.copyOf(extractCoverIds(book.getCoverUrl())),
        authors,
        works,
        new ArrayList<>(book.getSubjects()));
  }

  static AuthorDto toAuthor(AuthorEntity author) {
    return new AuthorDto(
        author.getCatalogKey(),
        author.getName(),
        author.getBio(),
        author.getPersonalName(),
        new ArrayList<>(author.getPhotos()));
  }

  static AuthorWorksResponse toAuthorWorks(List<BookEntity> books) {
    return new AuthorWorksResponse(books.stream().map(CatalogEntityMapper::toSummary).toList());
  }

  private static String firstText(String... values) {
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value;
      }
    }
    return "No description available.";
  }

  private static List<Long> extractCoverIds(String coverUrl) {
    if (coverUrl == null || coverUrl.isBlank()) {
      return List.of();
    }
    String normalized = coverUrl.trim();
    java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("/covers/(?:id|isbn|olid)/([^/?#]+)").matcher(normalized);
    if (!matcher.find()) {
      return List.of();
    }
    String value = matcher.group(1);
    try {
      return List.of(Long.parseLong(value));
    } catch (NumberFormatException ex) {
      return List.of();
    }
  }
}
