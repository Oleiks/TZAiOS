package org.example.library.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.example.library.dto.wishlist.WishlistItemDto;
import org.example.library.dto.wishlist.WishlistUpsertRequest;
import org.example.library.entity.AppUser;
import org.example.library.entity.WishlistItem;
import org.example.library.repository.AppUserRepository;
import org.example.library.repository.WishlistItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class WishlistService {
  private final WishlistItemRepository repository;
  private final AppUserRepository users;

  @Transactional(readOnly = true)
  public List<WishlistItemDto> list(UUID userId) {
    return repository.findAllByUserIdOrderByAddedAtDesc(userId).stream().map(this::toDto).toList();
  }

  @Transactional
  public WishlistItemDto add(UUID userId, WishlistUpsertRequest request) {
    AppUser user = loadUser(userId);
    WishlistItem item = repository.findByUserIdAndBookKeyIgnoreCase(userId, request.bookKey())
        .orElseGet(WishlistItem::new);

    item.setUser(user);
    item.setBookKey(request.bookKey().trim());
    item.setTitle(request.title().trim());
    item.setAuthorName(request.authorName().trim());
    item.setCoverUrl(request.coverUrl() == null || request.coverUrl().isBlank() ? null : request.coverUrl().trim());
    item.setBookJson(request.bookJson().trim());
    if (item.getAddedAt() == null) {
      item.setAddedAt(Instant.now());
    }

    return toDto(repository.save(item));
  }

  @Transactional
  public void remove(UUID userId, UUID id) {
    repository.deleteByIdAndUserId(id, userId);
  }

  private AppUser loadUser(UUID userId) {
    return users.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }

  private WishlistItemDto toDto(WishlistItem item) {
    return new WishlistItemDto(
        item.getId(),
        item.getBookKey(),
        item.getTitle(),
        item.getAuthorName(),
        item.getCoverUrl(),
        item.getBookJson(),
        item.getAddedAt());
  }
}
