package org.example.library.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.example.library.entity.AuthorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthorRepository extends JpaRepository<AuthorEntity, UUID> {
  Optional<AuthorEntity> findByCatalogKey(String catalogKey);

  @Query("select a from AuthorEntity a where lower(a.name) like lower(concat('%', :term, '%')) order by a.name asc")
  List<AuthorEntity> searchByName(@Param("term") String term);
}
