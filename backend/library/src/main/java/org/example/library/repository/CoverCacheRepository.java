package org.example.library.repository;

import java.util.Optional;
import java.util.UUID;
import org.example.library.entity.CoverCacheEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoverCacheRepository extends JpaRepository<CoverCacheEntry, UUID> {
  Optional<CoverCacheEntry> findByCacheKey(String cacheKey);
}
