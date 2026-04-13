package org.example.library.dto.catalog;

import java.util.List;

public record AuthorDto(
    String key,
    String name,
    String bio,
    String personalName,
    List<Long> photos
) {}
