package org.example.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "catalog_books")
public class BookEntity {
  public BookEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "catalog_key", nullable = false, unique = true, length = 255)
  private String catalogKey;

  @Column(nullable = false, length = 512)
  private String title;

  @Column(name = "primary_author_name", columnDefinition = "text")
  private String primaryAuthorName;

  @Column(columnDefinition = "text")
  private String description;

  @Column(name = "first_sentence", columnDefinition = "text")
  private String firstSentence;

  @Column(columnDefinition = "text")
  private String notes;

  @Column(columnDefinition = "text")
  private String excerpt;

  @Column(name = "cover_url", columnDefinition = "text")
  private String coverUrl;

  @Column(name = "first_publish_year")
  private Integer firstPublishYear;

  @Column
  private Double rating;

  @Column(name = "edition_count", nullable = false)
  private int editionCount;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "catalog_book_authors",
      joinColumns = @JoinColumn(name = "book_id"),
      inverseJoinColumns = @JoinColumn(name = "author_id")
  )
  private Set<AuthorEntity> authors = new LinkedHashSet<>();

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "catalog_book_subjects", joinColumns = @JoinColumn(name = "book_id"))
  @Column(name = "subject", nullable = false)
  private Set<String> subjects = new LinkedHashSet<>();

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "catalog_book_work_keys", joinColumns = @JoinColumn(name = "book_id"))
  @Column(name = "work_key", nullable = false)
  private Set<String> workKeys = new LinkedHashSet<>();
}
