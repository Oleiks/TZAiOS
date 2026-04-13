package org.example.library.dto.wishlist;

import java.time.Instant;
import java.util.UUID;

public record WishlistItemDto(
    UUID id,
    String bookKey,
    String title,
    String authorName,
    String coverUrl,
    String bookJson,
    Instant addedAt
) {}
