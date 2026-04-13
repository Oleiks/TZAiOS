package org.example.library.dto.catalog;

import java.util.List;

public record BookDetailsDto(
    String key,
    String title,
    String description,
    String firstSentence,
    String notes,
    String excerpt,
    String coverUrl,
    List<Long> covers,
    List<BookAuthorRefDto> authors,
    List<BookWorkRefDto> works,
    List<String> subjects
) {}
