package org.example.library.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.example.library.dto.wishlist.WishlistItemDto;
import org.example.library.dto.wishlist.WishlistUpsertRequest;
import org.example.library.service.WishlistService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wishlist")
public class WishlistController {
  private final WishlistService wishlistService;

  public WishlistController(WishlistService wishlistService) {
    this.wishlistService = wishlistService;
  }

  @GetMapping
  public List<WishlistItemDto> list(Authentication authentication) {
    return wishlistService.list(userId(authentication));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public WishlistItemDto add(Authentication authentication, @Valid @RequestBody WishlistUpsertRequest request) {
    return wishlistService.add(userId(authentication), request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void remove(Authentication authentication, @PathVariable UUID id) {
    wishlistService.remove(userId(authentication), id);
  }

  private UUID userId(Authentication authentication) {
    return UUID.fromString(authentication.getName());
  }
}
