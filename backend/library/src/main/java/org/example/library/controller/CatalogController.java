package org.example.library.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.library.dto.catalog.HomeResponse;
import org.example.library.dto.catalog.SearchResponse;
import org.example.library.dto.catalog.SubjectResponse;
import org.example.library.service.CatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/v1")
public class CatalogController {
  private final CatalogService catalogService;

  public CatalogController(CatalogService catalogService) {
    this.catalogService = catalogService;
  }

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
  public JsonNode book(@RequestParam String key) {
    return catalogService.book(key);
  }

  @GetMapping("/books/editions")
  public JsonNode editions(@RequestParam String key) {
    return catalogService.editions(key);
  }

  @GetMapping("/authors")
  public JsonNode author(@RequestParam String key) {
    return catalogService.author(key);
  }

  @GetMapping("/authors/works")
  public JsonNode authorWorks(@RequestParam String key) {
    return catalogService.authorWorks(key);
  }
}
