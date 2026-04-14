package org.example.library.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.example.library.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<BookEntity, UUID> {
  Optional<BookEntity> findByCatalogKey(String catalogKey);

  @Query("select distinct b from BookEntity b where lower(b.title) like lower(concat('%', :term, '%')) or lower(coalesce(b.primaryAuthorName, '')) like lower(concat('%', :term, '%')) order by b.title asc")
  List<BookEntity> search(@Param("term") String term);

  @Query("select distinct b from BookEntity b join b.subjects s where lower(s) = lower(:subject) order by b.title asc")
  List<BookEntity> findBySubject(@Param("subject") String subject);

  @Query("select distinct b from BookEntity b join b.authors a where a.catalogKey = :authorKey order by b.title asc")
  List<BookEntity> findByAuthorKey(@Param("authorKey") String authorKey);
}
