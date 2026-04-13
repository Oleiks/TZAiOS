package org.example.library.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;

import lombok.NoArgsConstructor;
import org.example.library.dto.catalog.AuthorDto;
import org.example.library.dto.catalog.AuthorRefDto;
import org.example.library.dto.catalog.AuthorWorksResponse;
import org.example.library.dto.catalog.BookAuthorRefDto;
import org.example.library.dto.catalog.BookDetailsDto;
import org.example.library.dto.catalog.BookSummaryDto;
import org.example.library.dto.catalog.BookWorkRefDto;

@NoArgsConstructor
public final class OpenLibraryMapper {

  public static BookSummaryDto fromSearchDoc(JsonNode doc) {
    return new BookSummaryDto(
        value(doc, "key", value(doc, "cover_edition_key", null)),
        doc.path("key").asText(null) != null ? doc.path("key").asText() : (value(doc, "cover_edition_key", null) != null ? "/books/" + value(doc, "cover_edition_key", null) : null),
        value(doc, "title", "Untitled"),
        authorName(doc),
        authorKey(doc),
        doc.hasNonNull("first_publish_year") ? doc.path("first_publish_year").asInt() : null,
        doc.hasNonNull("ratings_average") ? doc.path("ratings_average").asDouble() : null,
        coverUrl(doc),
        listOfStrings(doc.path("subject"), 4),
        doc.hasNonNull("edition_count") ? doc.path("edition_count").asInt() : 0);
  }

  public static BookSummaryDto fromSubjectWork(JsonNode work) {
    String authorKey = authorKey(work);
    String authorName = authorName(work);
    return new BookSummaryDto(
        value(work, "key", null),
        value(work, "key", null),
        value(work, "title", "Untitled"),
        authorName,
        authorKey,
        work.hasNonNull("first_publish_year") ? work.path("first_publish_year").asInt() : null,
        work.hasNonNull("rating") ? work.path("rating").asDouble() : null,
        coverUrl(work),
        listOfStrings(work.path("subject"), 4),
        0);
  }

  public static List<BookSummaryDto> fromDocs(JsonNode docs) {
    List<BookSummaryDto> books = new ArrayList<>();
    if (docs != null && docs.isArray()) {
      for (JsonNode doc : docs) {
        books.add(fromSearchDoc(doc));
      }
    }
    return books;
  }

  public static BookDetailsDto fromBookDetails(JsonNode book) {
    return new BookDetailsDto(
        value(book, "key", null),
        value(book, "title", "Untitled"),
        proseText(book.path("description"), book.path("first_sentence"), book.path("notes"), book.path("excerpt"), book.path("excerpts")),
        textValue(book, "first_sentence"),
        textValue(book, "notes"),
        textValue(book, "excerpt"),
        coverUrl(book),
        longValues(book.path("covers"), 12),
        bookAuthors(book.path("authors")),
        bookWorks(book.path("works")),
        listOfStrings(book.path("subjects"), 10));
  }

  public static AuthorDto fromAuthor(JsonNode author) {
    return new AuthorDto(
        value(author, "key", null),
        value(author, "name", "Unknown author"),
        proseText(author.path("bio")),
        value(author, "personal_name", null),
        longValues(author.path("photos"), 12));
  }

  public static AuthorWorksResponse fromAuthorWorks(JsonNode payload) {
    List<BookSummaryDto> entries = new ArrayList<>();
    if (payload != null) {
      JsonNode array = payload.path("entries");
      if (array.isArray()) {
        for (JsonNode work : array) {
          entries.add(fromSubjectWork(work));
        }
      } else if (payload.isArray()) {
        for (JsonNode work : payload) {
          entries.add(fromSubjectWork(work));
        }
      }
    }
    return new AuthorWorksResponse(entries);
  }

  private static String coverUrl(JsonNode doc) {
    if (doc.hasNonNull("cover_i")) {
      long value = doc.path("cover_i").asLong(-1);
      if (value > 0) {
        return "/covers/id/" + value + "?size=M";
      }
    }
    if (doc.hasNonNull("cover_edition_key")) {
      String value = doc.path("cover_edition_key").asText("").trim();
      if (!value.isEmpty() && !"-1".equals(value)) {
        return "/covers/olid/" + value + "?size=M";
      }
    }
    if (doc.path("isbn").isArray() && !doc.path("isbn").isEmpty()) {
      String value = doc.path("isbn").get(0).asText("").trim();
      if (!value.isEmpty() && !"-1".equals(value)) {
        return "/covers/isbn/" + value + "?size=M";
      }
    }
    if (doc.hasNonNull("cover_id")) {
      long value = doc.path("cover_id").asLong(-1);
      if (value > 0) {
        return "/covers/id/" + value + "?size=M";
      }
    }
    if (doc.path("covers").isArray() && !doc.path("covers").isEmpty()) {
      long value = doc.path("covers").get(0).asLong(-1);
      if (value > 0) {
        return "/covers/id/" + value + "?size=M";
      }
    }
    return null;
  }

  private static List<BookAuthorRefDto> bookAuthors(JsonNode array) {
    List<BookAuthorRefDto> authors = new ArrayList<>();
    if (array != null && array.isArray()) {
      for (JsonNode node : array) {
        JsonNode author = node.path("author");
        String key = author.hasNonNull("key") ? author.path("key").asText() : node.path("key").asText(null);
        if (key != null && !key.isBlank()) {
          authors.add(new BookAuthorRefDto(new AuthorRefDto(key, value(author, "name", null))));
        }
      }
    }
    return authors;
  }

  private static List<BookWorkRefDto> bookWorks(JsonNode array) {
    List<BookWorkRefDto> works = new ArrayList<>();
    if (array != null && array.isArray()) {
      for (JsonNode node : array) {
        String key = value(node, "key", null);
        if (key != null && !key.isBlank()) {
          works.add(new BookWorkRefDto(key));
        }
      }
    }
    return works;
  }

  private static List<Long> longValues(JsonNode array, int limit) {
    List<Long> values = new ArrayList<>();
    if (array != null && array.isArray()) {
      for (int i = 0; i < Math.min(limit, array.size()); i++) {
        JsonNode node = array.get(i);
        if (node != null && node.canConvertToLong()) {
          values.add(node.asLong());
        }
      }
    }
    return values;
  }

  private static String authorName(JsonNode doc) {
    if (doc.path("author_name").isArray() && !doc.path("author_name").isEmpty()) {
      return doc.path("author_name").get(0).asText("Unknown author");
    }
    if (doc.path("authors").isArray() && !doc.path("authors").isEmpty()) {
      JsonNode first = doc.path("authors").get(0);
      if (first.hasNonNull("name")) {
        return first.path("name").asText("Unknown author");
      }
      if (first.hasNonNull("key")) {
        String key = first.path("key").asText();
        int index = key.lastIndexOf('/');
        return index >= 0 && index + 1 < key.length() ? key.substring(index + 1) : key;
      }
    }
    return "Unknown author";
  }

  private static String authorKey(JsonNode doc) {
    if (doc.path("author_key").isArray() && !doc.path("author_key").isEmpty()) {
      return "/authors/" + doc.path("author_key").get(0).asText();
    }
    if (doc.path("authors").isArray() && !doc.path("authors").isEmpty()) {
      JsonNode first = doc.path("authors").get(0);
      if (first.hasNonNull("key")) {
        return first.path("key").asText();
      }
    }
    return null;
  }

  private static List<String> listOfStrings(JsonNode array, int limit) {
    List<String> values = new ArrayList<>();
    if (array != null && array.isArray()) {
      for (int i = 0; i < Math.min(limit, array.size()); i++) {
        values.add(array.get(i).asText());
      }
    }
    return values;
  }

  private static String firstText(JsonNode array, String fallback) {
    if (array != null && array.isArray() && !array.isEmpty()) {
      return array.get(0).asText(fallback);
    }
    return fallback;
  }

  private static String value(JsonNode node, String field, String fallback) {
    return node.hasNonNull(field) ? node.path(field).asText() : fallback;
  }

  private static String textValue(JsonNode node, String field) {
    if (node == null || !node.has(field)) {
      return "";
    }
    JsonNode value = node.get(field);
    if (value == null || value.isNull()) {
      return "";
    }
    if (value.isTextual()) {
      return value.asText("").trim();
    }
    if (value.hasNonNull("value")) {
      return value.path("value").asText("").trim();
    }
    if (value.hasNonNull("excerpt")) {
      return value.path("excerpt").asText("").trim();
    }
    if (value.hasNonNull("text")) {
      return value.path("text").asText("").trim();
    }
    return value.asText("").trim();
  }

  private static String proseText(JsonNode... nodes) {
    for (JsonNode node : nodes) {
      String text = textNode(node);
      if (!text.isBlank()) {
        return text;
      }
    }
    return "";
  }

  private static String textNode(JsonNode node) {
    if (node == null || node.isNull()) {
      return "";
    }
    if (node.isArray()) {
      for (JsonNode item : node) {
        String text = textNode(item);
        if (!text.isBlank()) {
          return text;
        }
      }
      return "";
    }
    if (node.isTextual()) {
      return node.asText("").trim();
    }
    if (node.hasNonNull("value")) {
      return node.path("value").asText("").trim();
    }
    if (node.hasNonNull("excerpt")) {
      return node.path("excerpt").asText("").trim();
    }
    if (node.hasNonNull("text")) {
      return node.path("text").asText("").trim();
    }
    return node.asText("").trim();
  }
}
