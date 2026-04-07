package org.example.library.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import org.example.library.dto.catalog.BookSummaryDto;

public final class OpenLibraryMapper {
  private OpenLibraryMapper() {}

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

  private static String coverUrl(JsonNode doc) {
    if (doc.hasNonNull("cover_i")) {
      return "/covers/id/" + doc.path("cover_i").asText() + "?size=M";
    }
    if (doc.path("isbn").isArray() && !doc.path("isbn").isEmpty()) {
      return "/covers/isbn/" + doc.path("isbn").get(0).asText() + "?size=M";
    }
    if (doc.hasNonNull("cover_id")) {
      return "/covers/id/" + doc.path("cover_id").asText() + "?size=M";
    }
    if (doc.path("covers").isArray() && !doc.path("covers").isEmpty()) {
      return "/covers/id/" + doc.path("covers").get(0).asText() + "?size=M";
    }
    return null;
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
}
