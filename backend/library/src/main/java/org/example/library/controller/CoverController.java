package org.example.library.controller;

import lombok.RequiredArgsConstructor;
import org.example.library.service.CoverProxyService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.Duration;

@RestController
@RequestMapping("/api/v1/covers")
@RequiredArgsConstructor
public class CoverController {
  private final CoverProxyService coverProxyService;


  @GetMapping("/id/{coverId}")
  public ResponseEntity<byte[]> byId(@PathVariable String coverId, @RequestParam(defaultValue = "M") String size) {
    var image = coverProxyService.fetchById(coverId, size);
    return cached(image);
  }

  @GetMapping("/olid/{olid}")
  public ResponseEntity<byte[]> byOlid(@PathVariable String olid, @RequestParam(defaultValue = "M") String size) {
    var image = coverProxyService.fetchByOlid(olid, size);
    return cached(image);
  }

  @GetMapping("/isbn/{isbn}")
  public ResponseEntity<byte[]> byIsbn(@PathVariable String isbn, @RequestParam(defaultValue = "M") String size) {
    var image = coverProxyService.fetchByIsbn(isbn, size);
    return cached(image);
  }

  private ResponseEntity<byte[]> cached(CoverProxyService.CoverImage image) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
        .contentType(image.contentType())
        .body(image.bytes());
  }
}
