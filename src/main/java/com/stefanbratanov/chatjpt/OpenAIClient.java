package com.stefanbratanov.chatjpt;

import static com.stefanbratanov.chatjpt.Utils.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Subclasses should be based on one of the endpoints defined at <a
 * href="https://platform.openai.com/docs/api-reference">API Reference</a>
 */
abstract class OpenAIClient {

  private final String apiKey;
  private final Optional<String> organization;

  protected final HttpClient httpClient;
  protected final ObjectMapper objectMapper;

  OpenAIClient(
      String apiKey,
      Optional<String> organization,
      HttpClient httpClient,
      ObjectMapper objectMapper) {
    this.apiKey = apiKey;
    this.organization = organization;
    this.httpClient = httpClient;
    this.objectMapper = objectMapper;
  }

  HttpRequest.Builder newHttpRequestBuilder(String... headers) {
    return HttpRequest.newBuilder()
        .headers(getAuthenticationHeaders(apiKey, organization))
        .headers(headers);
  }

  <T> HttpRequest.BodyPublisher createBodyPublisher(T body) {
    try {
      return HttpRequest.BodyPublishers.ofByteArray(objectMapper.writeValueAsBytes(body));
    } catch (JsonProcessingException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  HttpResponse<byte[]> sendHttpRequest(HttpRequest httpRequest) {
    try {
      HttpResponse<byte[]> httpResponse =
          httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
      validateHttpResponse(httpResponse, objectMapper);
      return httpResponse;
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    } catch (InterruptedException ex) {
      throw new RuntimeException(ex);
    }
  }

  CompletableFuture<HttpResponse<byte[]>> sendHttpRequestAsync(HttpRequest httpRequest) {
    return httpClient
        .sendAsync(httpRequest, HttpResponse.BodyHandlers.ofByteArray())
        .thenApply(
            httpResponse -> {
              validateHttpResponse(httpResponse, objectMapper);
              return httpResponse;
            });
  }

  <T> T deserializeResponse(byte[] response, Class<T> responseClass) {
    try {
      return objectMapper.readValue(response, responseClass);
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }
}