package org.example.library.dto.catalog;

import java.util.List;

public record SectionDto(String id, String title, List<BookSummaryDto> works) {}
