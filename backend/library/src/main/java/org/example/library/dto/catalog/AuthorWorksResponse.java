package org.example.library.dto.catalog;

import java.util.List;

public record AuthorWorksResponse(List<BookSummaryDto> entries) {}
