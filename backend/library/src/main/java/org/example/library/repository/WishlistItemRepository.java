package org.example.library.repository;

import java.util.List;
import java.util.UUID;
import org.example.library.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, UUID> {
  List<WishlistItem> findAllByUserIdOrderByAddedAtDesc(UUID userId);

  java.util.Optional<WishlistItem> findByUserIdAndBookKeyIgnoreCase(UUID userId, String bookKey);

  boolean existsByUserIdAndBookKeyIgnoreCase(UUID userId, String bookKey);

  void deleteByIdAndUserId(UUID id, UUID userId);
}
