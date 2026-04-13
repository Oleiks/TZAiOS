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
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
  public RefreshToken() {}

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private AppUser user;

  @Column(name = "token_hash", nullable = false, unique = true, length = 128)
  private String tokenHash;

  @Column(name = "created_at", nullable = false, columnDefinition = "timestamp with time zone")
  private Instant createdAt;

  @Column(name = "expires_at", nullable = false, columnDefinition = "timestamp with time zone")
  private Instant expiresAt;

  @Column(name = "revoked_at", columnDefinition = "timestamp with time zone")
  private Instant revokedAt;

}
