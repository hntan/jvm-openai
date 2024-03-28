package io.github.stefanbratanov.jvm.openai;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Usage statistics */
public record Usage(@JsonProperty("completion_tokens") int completionTokens, @JsonProperty("prompt_tokens") int promptTokens, @JsonProperty("total_tokens") int totalTokens) {}
