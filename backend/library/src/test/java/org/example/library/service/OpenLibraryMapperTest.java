package org.example.library.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.library.dto.catalog.AuthorDto;
import org.example.library.dto.catalog.BookDetailsDto;
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

  @Test
  void mapsCoverEditionKeyIntoSummaryCoverUrl() throws Exception {
    BookSummaryDto dto = OpenLibraryMapper.fromSearchDoc(new ObjectMapper().readTree("""
        {
          "key": "/works/OL123W",
          "title": "Example Book",
          "cover_edition_key": "OL123M"
        }
        """));

    assertThat(dto.coverUrl()).isEqualTo("/covers/olid/OL123M?size=M");
  }

  @Test
  void ignoresInvalidCoverIds() throws Exception {
    BookSummaryDto dto = OpenLibraryMapper.fromSearchDoc(new ObjectMapper().readTree("""
        {
          "key": "/works/OL123W",
          "title": "Example Book",
          "cover_i": -1
        }
        """));

    assertThat(dto.coverUrl()).isNull();
  }

  @Test
  void mapsBookDetailsIntoDto() throws Exception {
    BookDetailsDto dto = OpenLibraryMapper.fromBookDetails(new ObjectMapper().readTree("""
        {
          "key": "/works/OL123W",
          "title": "Example Book",
          "description": { "value": "A real description." },
          "first_sentence": { "value": "First sentence." },
          "covers": [12],
          "authors": [{ "author": { "key": "/authors/OL1A" } }],
          "works": [{ "key": "/works/OL123W" }],
          "subjects": ["fiction"]
        }
        """));

    assertThat(dto.description()).isEqualTo("A real description.");
    assertThat(dto.coverUrl()).isEqualTo("/covers/id/12?size=M");
    assertThat(dto.authors()).hasSize(1);
    assertThat(dto.authors().get(0).author().key()).isEqualTo("/authors/OL1A");
    assertThat(dto.works().get(0).key()).isEqualTo("/works/OL123W");
  }

  @Test
  void mapsAuthorIntoDto() throws Exception {
    AuthorDto dto = OpenLibraryMapper.fromAuthor(new ObjectMapper().readTree("""
        {
          "key": "/authors/OL1A",
          "name": "Jane Doe",
          "bio": { "value": "Author bio." },
          "photos": [42]
        }
        """));

    assertThat(dto.name()).isEqualTo("Jane Doe");
    assertThat(dto.bio()).isEqualTo("Author bio.");
    assertThat(dto.photos()).containsExactly(42L);
  }
}
