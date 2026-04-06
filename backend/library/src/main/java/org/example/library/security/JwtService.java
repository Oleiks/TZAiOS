package org.example.library.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.example.library.config.LibraryProperties;
import org.example.library.entity.AppUser;
import org.springframework.stereotype.Component;

@Component
public class JwtService {
  private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
  private static final String HMAC_ALG = "HmacSHA256";

  private final ObjectMapper objectMapper;
  private final byte[] secret;
  private final java.time.Duration accessTokenTtl;

  public JwtService(ObjectMapper objectMapper, LibraryProperties properties) {
    this.objectMapper = objectMapper;
    this.secret = properties.security().jwtSecret().getBytes(StandardCharsets.UTF_8);
    this.accessTokenTtl = properties.security().accessTokenTtl();
  }

  public String createAccessToken(AppUser user) {
    Instant now = Instant.now();
    return createToken(user.getId().toString(), user.getId().toString(), user.getEmail(), user.getDisplayName(), now, now.plus(accessTokenTtl));
  }

  public JwtClaims verifyAccessToken(String token) {
    try {
      String[] parts = token.split("\\.");
      if (parts.length != 3) {
        throw new IllegalArgumentException("Invalid token");
      }
      String signed = parts[0] + "." + parts[1];
      byte[] expected = hmac(signed.getBytes(StandardCharsets.UTF_8));
      byte[] actual = DECODER.decode(parts[2]);
      if (!MessageDigest.isEqual(expected, actual)) {
        throw new IllegalArgumentException("Invalid token signature");
      }
      JsonNode payload = objectMapper.readTree(DECODER.decode(parts[1]));
      Instant expiresAt = Instant.ofEpochSecond(payload.get("exp").asLong());
      if (Instant.now().isAfter(expiresAt)) {
        throw new IllegalArgumentException("Token expired");
      }
      return new JwtClaims(payload.get("sub").asText(), payload.get("uid").asText(), payload.get("email").asText(), payload.get("name").asText());
    } catch (Exception ex) {
      throw new IllegalArgumentException("Invalid token", ex);
    }
  }

  private String createToken(String subject, String userId, String email, String name, Instant issuedAt, Instant expiresAt) {
    try {
      var headerJson = objectMapper.writeValueAsBytes(java.util.Map.of("alg", "HS256", "typ", "JWT"));
      var payloadJson = objectMapper.writeValueAsBytes(java.util.Map.of(
          "sub", subject,
          "uid", userId,
          "email", email,
          "name", name,
          "iat", issuedAt.getEpochSecond(),
          "exp", expiresAt.getEpochSecond()));
      String header = ENCODER.encodeToString(headerJson);
      String payload = ENCODER.encodeToString(payloadJson);
      String signature = ENCODER.encodeToString(hmac((header + "." + payload).getBytes(StandardCharsets.UTF_8)));
      return header + "." + payload + "." + signature;
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to create token", ex);
    }
  }

  private byte[] hmac(byte[] data) throws Exception {
    Mac mac = Mac.getInstance(HMAC_ALG);
    mac.init(new SecretKeySpec(secret, HMAC_ALG));
    return mac.doFinal(data);
  }

  public record JwtClaims(String subject, String userId, String email, String name) {}
}
