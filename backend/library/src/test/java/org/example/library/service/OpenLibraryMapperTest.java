package org.example.library.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.library.dto.catalog.BookSummaryDto;
import org.junit.jupiter.api.Test;

class OpenLibraryMapperTest {
  @Test
  void mapsSearchDocIntoSummary() throws Exception {
    String json = """
        {
          "key": "/works/OL123W",
          "title": "Example Book",
          "author_name": ["Jane Doe"],
          "author_key": ["OL1A"],
          "first_publish_year": 1999,
          "cover_i": 12,
          "subject": ["fiction", "adventure"],
          "edition_count": 4
        }
        """;
    BookSummaryDto dto = OpenLibraryMapper.fromSearchDoc(new ObjectMapper().readTree(json));

    assertThat(dto.title()).isEqualTo("Example Book");
    assertThat(dto.authorName()).isEqualTo("Jane Doe");
    assertThat(dto.workKey()).isEqualTo("/works/OL123W");
    assertThat(dto.coverUrl()).isEqualTo("/covers/id/12?size=M");
  }
}
