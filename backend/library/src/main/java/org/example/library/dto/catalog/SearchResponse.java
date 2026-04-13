package org.example.library.dto.catalog;

import java.util.List;

public record SearchResponse(long numFound, List<BookSummaryDto> books) {}
