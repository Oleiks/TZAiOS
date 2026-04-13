package org.example.library.controller;

import lombok.RequiredArgsConstructor;
import org.example.library.dto.catalog.HomeResponse;
import org.example.library.dto.catalog.SearchResponse;
import org.example.library.dto.catalog.SubjectResponse;
import org.example.library.dto.catalog.AuthorDto;
import org.example.library.dto.catalog.AuthorWorksResponse;
import org.example.library.dto.catalog.BookDetailsDto;
import org.example.library.service.CatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CatalogController {
  private final CatalogService catalogService;

  @GetMapping("/home")
  public HomeResponse home() {
    return catalogService.home();
  }

  @GetMapping("/search")
  public SearchResponse search(@RequestParam String q, @RequestParam(defaultValue = "1") int page) {
    return catalogService.search(q, page);
  }

  @GetMapping("/subjects/{subject}")
  public SubjectResponse subject(@PathVariable String subject) {
    return catalogService.subject(subject);
  }

  @GetMapping("/books")
  public BookDetailsDto book(@RequestParam String key) {
    return catalogService.book(key);
  }

  @GetMapping("/authors")
  public AuthorDto author(@RequestParam String key) {
    return catalogService.author(key);
  }

  @GetMapping("/authors/works")
  public AuthorWorksResponse authorWorks(@RequestParam String key, @RequestParam(defaultValue = "10") int limit) {
    return catalogService.authorWorks(key, limit);
  }
}
