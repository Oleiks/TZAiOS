package org.example.library.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CoverProxyService {
  private static final Logger log = LoggerFactory.getLogger(CoverProxyService.class);
  private static final URI BASE_URI = URI.create("https://covers.openlibrary.org");

  private final HttpClient httpClient = HttpClient.newBuilder()
      .followRedirects(HttpClient.Redirect.ALWAYS)
      .build();

  public CoverImage fetchById(String coverId, String size) {
    return fetch("/b/id/" + coverId + "-" + normalizeSize(size) + ".jpg");
  }

  public CoverImage fetchByIsbn(String isbn, String size) {
    return fetch("/b/isbn/" + isbn + "-" + normalizeSize(size) + ".jpg");
  }

  private CoverImage fetch(String path) {
    log.info("Proxy cover fetch [{}]", path);
    try {
      HttpResponse<byte[]> response = httpClient.send(
          HttpRequest.newBuilder(BASE_URI.resolve(path))
              .GET()
              .header("Accept", "image/*")
              .build(),
          HttpResponse.BodyHandlers.ofByteArray());

      if (response.statusCode() != 200 || response.body() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to fetch cover image");
      }

      String contentType = response.headers().firstValue("content-type").orElse(MediaType.IMAGE_JPEG_VALUE);
      log.info("Cover fetch completed [{}] status={}", path, response.statusCode());
      return new CoverImage(response.body(), MediaType.parseMediaType(contentType));
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cover request interrupted", ex);
    } catch (IOException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to fetch cover image", ex);
    }
  }

  private String normalizeSize(String size) {
    String value = size == null ? "M" : size.trim().toUpperCase(Locale.ROOT);
    return switch (value) {
      case "S", "M", "L" -> value;
      default -> "M";
    };
  }

  public record CoverImage(byte[] bytes, MediaType contentType) {}
}
