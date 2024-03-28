package io.github.stefanbratanov.jvm.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record Images(long created, List<Image> data) {

  public record Image(String b64Json, String url, @JsonProperty("revised_prompt") String revisedPrompt) {}
}
