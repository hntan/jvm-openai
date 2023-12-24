package com.stefanbratanov.chatjpt;

import static com.stefanbratanov.chatjpt.Utils.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

public final class ChatJPT {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  static {
    SimpleModule module = new SimpleModule();
    module
        .addSerializer(ChatRequest.class, new ChatRequestSerializer())
        .addDeserializer(ChatResponse.class, new ChatResponseDeserializer())
        .addDeserializer(Model.class, new ModelDeserializer())
        .addDeserializer(Error.class, new ErrorDeserializer());
    OBJECT_MAPPER.registerModule(module);
  }

  private final URI baseUrl;
  private final String apiKey;
  private final Optional<String> organization;
  private final HttpClient httpClient;

  private ChatJPT(
      URI baseUrl, String apiKey, Optional<String> organization, HttpClient httpClient) {
    this.baseUrl = baseUrl;
    this.apiKey = apiKey;
    this.organization = organization;
    this.httpClient = httpClient;
  }

  public ChatClient newChatClient() {
    return new ChatClient(baseUrl, apiKey, organization, httpClient, OBJECT_MAPPER);
  }

  public List<Model> models() {
    HttpRequest httpRequest =
        HttpRequest.newBuilder()
            .headers(getAuthenticationHeaders(apiKey, organization))
            .uri(baseUrl.resolve(Endpoint.MODELS.getPath()))
            .GET()
            .build();
    try {
      HttpResponse<byte[]> httpResponse =
          httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
      validateHttpResponse(httpResponse, OBJECT_MAPPER);
      JsonNode models = OBJECT_MAPPER.readTree(httpResponse.body());
      return OBJECT_MAPPER.readValue(models.get("data").toString(), new TypeReference<>() {});
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    } catch (InterruptedException ex) {
      throw new RuntimeException(ex);
    }
  }

  public Model model(String model) {
    HttpRequest httpRequest =
        HttpRequest.newBuilder()
            .headers(getAuthenticationHeaders(apiKey, organization))
            .uri(baseUrl.resolve(Endpoint.MODELS.getPath() + "/" + model))
            .GET()
            .build();
    try {
      HttpResponse<byte[]> httpResponse =
          httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
      validateHttpResponse(httpResponse, OBJECT_MAPPER);
      return OBJECT_MAPPER.readValue(httpResponse.body(), Model.class);
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    } catch (InterruptedException ex) {
      throw new RuntimeException(ex);
    }
  }

  public static Builder newBuilder(String apiKey) {
    return new Builder(apiKey);
  }

  public static class Builder {

    private static final String DEFAULT_BASE_URL = "https://api.openai.com/v1/";

    private String baseUrl = DEFAULT_BASE_URL;
    private final String apiKey;
    private Optional<String> organization = Optional.empty();

    public Builder(String apiKey) {
      this.apiKey = apiKey;
    }

    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public Builder organization(String organization) {
      this.organization = Optional.of(organization);
      return this;
    }

    public ChatJPT build() {
      if (!baseUrl.endsWith("/")) {
        baseUrl += "/";
      }
      HttpClient httpClient = HttpClient.newBuilder().build();
      return new ChatJPT(URI.create(baseUrl), apiKey, organization, httpClient);
    }
  }
}