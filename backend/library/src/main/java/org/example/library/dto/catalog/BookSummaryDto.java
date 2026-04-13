package org.example.library.dto.catalog;

import java.util.List;

public record BookSummaryDto(
    String key,
    String workKey,
    String title,
    String authorName,
    String authorKey,
    Integer firstPublishYear,
    Double rating,
    String coverUrl,
    List<String> subjects,
    int editionCount
) {}
