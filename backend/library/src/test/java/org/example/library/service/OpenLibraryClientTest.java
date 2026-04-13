package org.example.library.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class OpenLibraryClientTest {
  @Test
  void mergesEditionDescriptionIntoWorkPayload() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    var work = mapper.readTree("""
        {"key":"/works/OL1W","title":"Work Title"}
        """);
    var edition = mapper.readTree("""
        {"key":"/books/OL1M","excerpt":{"type":"/type/text","value":"Edition description"},"cover_edition_key":"OL1M","covers":[12]}
        """);

    var merged = OpenLibraryClient.mergeWorkPayload(work, edition);

    assertThat(merged.path("excerpt").path("value").asText()).isEqualTo("Edition description");
    assertThat(merged.path("cover_edition_key").asText()).isEqualTo("OL1M");
    assertThat(merged.path("covers").get(0).asInt()).isEqualTo(12);
  }
}
