package org.example.library.service;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.example.library.config.LibraryProperties;
import org.example.library.dto.auth.AuthResponse;
import org.example.library.dto.auth.LoginRequest;
import org.example.library.dto.auth.RefreshRequest;
import org.example.library.dto.auth.RegisterRequest;
import org.example.library.dto.auth.UserDto;
import org.example.library.entity.AppUser;
import org.example.library.entity.RefreshToken;
import org.example.library.repository.AppUserRepository;
import org.example.library.repository.RefreshTokenRepository;
import org.example.library.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {
  private static final SecureRandom RANDOM = new SecureRandom();

  private final AppUserRepository users;
  private final RefreshTokenRepository refreshTokens;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final LibraryProperties properties;

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    if (users.existsByEmailIgnoreCase(request.email())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
    }

    AppUser user = new AppUser();
    user.setEmail(request.email().trim().toLowerCase());
    user.setPasswordHash(passwordEncoder.encode(request.password()));
    user.setDisplayName(request.displayName().trim());
    user.setCreatedAt(Instant.now());
    users.save(user);

    return issueTokens(user);
  }

  @Transactional
  public AuthResponse login(LoginRequest request) {
    AppUser user = users.findByEmailIgnoreCase(request.email().trim())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }

    return issueTokens(user);
  }

  @Transactional
  public AuthResponse refresh(RefreshRequest request) {
    RefreshToken token = refreshTokens.findByTokenHash(hashToken(request.refreshToken()))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

    Instant now = Instant.now();
    if (token.getRevokedAt() != null || now.isAfter(token.getExpiresAt())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
    }

    token.setRevokedAt(now);
    refreshTokens.save(token);
    return issueTokens(token.getUser());
  }

  @Transactional(readOnly = true)
  public UserDto me(UUID userId) {
    AppUser user = users.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    return toUserDto(user);
  }

  private AuthResponse issueTokens(AppUser user) {
    String refreshToken = generateRefreshToken();
    RefreshToken entity = new RefreshToken();
    entity.setUser(user);
    entity.setTokenHash(hashToken(refreshToken));
    entity.setCreatedAt(Instant.now());
    entity.setExpiresAt(Instant.now().plus(properties.security().refreshTokenTtl()));
    refreshTokens.save(entity);

    return new AuthResponse(jwtService.createAccessToken(user), refreshToken, toUserDto(user));
  }

  private UserDto toUserDto(AppUser user) {
    return new UserDto(user.getId(), user.getEmail(), user.getDisplayName());
  }

  private String generateRefreshToken() {
    byte[] bytes = new byte[48];
    RANDOM.nextBytes(bytes);
    return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String hashToken(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(token.getBytes(UTF_8)));
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to hash token", ex);
    }
  }
}
