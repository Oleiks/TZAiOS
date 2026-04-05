package org.example.library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
  @Bean
  RestClient openLibraryRestClient(LibraryProperties properties) {
    return RestClient.builder().baseUrl(properties.openapi().baseUrl()).build();
  }
}
