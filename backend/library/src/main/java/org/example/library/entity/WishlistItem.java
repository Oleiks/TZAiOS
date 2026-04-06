package org.example.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "wishlist_items")
public class WishlistItem {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private AppUser user;

  @Column(name = "book_key", nullable = false, length = 255)
  private String bookKey;

  @Column(nullable = false)
  private String title;

  @Column(name = "author_name", nullable = false)
  private String authorName;

  @Column(name = "cover_url")
  private String coverUrl;

  @Column(name = "book_json", nullable = false, columnDefinition = "text")
  private String bookJson;

  @Column(nullable = false)
  private Instant addedAt;
}
