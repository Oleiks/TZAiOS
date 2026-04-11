package org.example.library.controller;

import org.example.library.service.CoverProxyService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/covers")
public class CoverController {
  private final CoverProxyService coverProxyService;

  public CoverController(CoverProxyService coverProxyService) {
    this.coverProxyService = coverProxyService;
  }

  @GetMapping("/id/{coverId}")
  public ResponseEntity<byte[]> byId(@PathVariable String coverId, @RequestParam(defaultValue = "M") String size) {
    var image = coverProxyService.fetchById(coverId, size);
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noCache())
        .contentType(image.contentType())
        .body(image.bytes());
  }

  @GetMapping("/isbn/{isbn}")
  public ResponseEntity<byte[]> byIsbn(@PathVariable String isbn, @RequestParam(defaultValue = "M") String size) {
    var image = coverProxyService.fetchByIsbn(isbn, size);
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noCache())
        .contentType(image.contentType())
        .body(image.bytes());
  }
}
