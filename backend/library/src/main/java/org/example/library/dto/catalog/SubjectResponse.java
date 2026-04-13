package org.example.library.dto.catalog;

import java.util.List;

public record SubjectResponse(String name, List<BookSummaryDto> works) {}
