package org.example.library.dto.wishlist;

import jakarta.validation.constraints.NotBlank;

public record WishlistUpsertRequest(
    @NotBlank String bookKey,
    @NotBlank String title,
    @NotBlank String authorName,
    String coverUrl,
    @NotBlank String bookJson
) {}
