package org.example.library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {
  @Bean
  RestClient openLibraryRestClient(LibraryProperties properties) {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(Duration.ofSeconds(5));
    factory.setReadTimeout(Duration.ofSeconds(10));

    return RestClient.builder()
        .requestFactory(factory)
        .baseUrl(properties.openapi().baseUrl())
        .defaultHeader("User-Agent", "idzi-library-backend (openlibrary-proxy)")
        .build();
  }
}
