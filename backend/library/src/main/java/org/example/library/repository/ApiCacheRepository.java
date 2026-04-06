package org.example.library.repository;

import java.util.Optional;
import java.util.UUID;
import org.example.library.entity.ApiCacheEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiCacheRepository extends JpaRepository<ApiCacheEntry, UUID> {
  Optional<ApiCacheEntry> findByCacheKey(String cacheKey);
}
