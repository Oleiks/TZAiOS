package org.example.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@Table(name = "catalog_authors")
public class AuthorEntity {
  public AuthorEntity() {}

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "catalog_key", nullable = false, unique = true, length = 255)
  private String catalogKey;

  @Column(nullable = false, length = 255)
  private String name;

  @Column(columnDefinition = "text")
  private String bio;

  @Column(name = "personal_name", columnDefinition = "text")
  private String personalName;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "catalog_author_photos", joinColumns = @JoinColumn(name = "author_id"))
  @Column(name = "photo_id", nullable = false)
  private Set<Long> photos = new LinkedHashSet<>();

  @ManyToMany(mappedBy = "authors", fetch = FetchType.LAZY)
  private Set<BookEntity> books = new LinkedHashSet<>();
}
