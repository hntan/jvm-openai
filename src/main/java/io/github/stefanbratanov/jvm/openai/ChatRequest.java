package io.github.stefanbratanov.jvm.openai;

import java.util.*;

/**
 * @param toolChoice {@link String} or {@link ToolChoice}
 */
public record ChatRequest(
    String model,
    List<ChatMessage> messages,
    Optional<Double> frequencyPenalty,
    Optional<Map<Integer, Integer>> logitBias,
    Optional<Boolean> logprobs,
    Optional<Integer> topLogprobs,
    Optional<Integer> maxTokens,
    Optional<Integer> n,
    Optional<Double> presencePenalty,
    Optional<ResponseFormat> responseFormat,
    Optional<Integer> seed,
    Optional<List<String>> stop,
    Optional<Boolean> stream,
    Optional<Double> temperature,
    Optional<Double> topP,
    Optional<List<Tool>> tools,
    Optional<Object> toolChoice,
    Optional<String> user) {

  public record ResponseFormat(String type) {
    public static ResponseFormat text() {
      return new ResponseFormat("text");
    }

    public static ResponseFormat json() {
      return new ResponseFormat("json_object");
    }
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private static final String DEFAULT_MODEL = "gpt-3.5-turbo";

    private String model = DEFAULT_MODEL;

    private final List<ChatMessage> messages = new LinkedList<>();

    private Optional<Double> frequencyPenalty = Optional.empty();
    private Optional<Map<Integer, Integer>> logitBias = Optional.empty();
    private Optional<Boolean> logprobs = Optional.empty();
    private Optional<Integer> topLogprobs = Optional.empty();
    private Optional<Integer> maxTokens = Optional.empty();
    private Optional<Integer> n = Optional.empty();
    private Optional<Double> presencePenalty = Optional.empty();
    private Optional<ResponseFormat> responseFormat = Optional.empty();
    private Optional<Integer> seed = Optional.empty();
    private final List<String> stop = new LinkedList<>();
    private Optional<Boolean> stream = Optional.empty();
    private Optional<Double> temperature = Optional.empty();
    private Optional<Double> topP = Optional.empty();
    private final List<Tool> tools = new LinkedList<>();
    private Optional<Object> toolChoice = Optional.empty();
    private Optional<String> user = Optional.empty();

    /**
     * @param model ID of the model to use
     */
    public Builder model(String model) {
      this.model = model;
      return this;
    }

    /**
     * @param message message to append to the list of messages comprising the conversation so far
     */
    public Builder message(ChatMessage message) {
      messages.add(message);
      return this;
    }

    /**
     * @param messages messages to append to the list of messages comprising the conversation so far
     */
    public Builder messages(List<ChatMessage> messages) {
      this.messages.addAll(messages);
      return this;
    }

    /**
     * @param frequencyPenalty Number between -2.0 and 2.0. Positive values penalize new tokens
     *     based on their existing frequency in the text so far, decreasing the model's likelihood
     *     to repeat the same line verbatim.
     */
    public Builder frequencyPenalty(double frequencyPenalty) {
      if (frequencyPenalty < -2 || frequencyPenalty > 2) {
        throw new IllegalArgumentException(
            "frequencyPenalty must be between -2.0 and 2.0 but it was " + frequencyPenalty);
      }
      this.frequencyPenalty = Optional.of(frequencyPenalty);
      return this;
    }

    /**
     * @param logitBias A map that maps tokens (specified by their token ID in the tokenizer) to an
     *     associated bias value from -100 to 100. Mathematically, the bias is added to the logits
     *     generated by the model prior to sampling. The exact effect will vary per model, but
     *     values between -1 and 1 should decrease or increase likelihood of selection; values like
     *     -100 or 100 should result in a ban or exclusive selection of the relevant token.
     */
    public Builder logitBias(Map<Integer, Integer> logitBias) {
      this.logitBias = Optional.of(logitBias);
      return this;
    }

    /**
     * @param logprobs Whether to return log probabilities of the output tokens or not. If true,
     *     returns the log probabilities of each output token returned in the content of message.
     */
    public Builder logprobs(boolean logprobs) {
      this.logprobs = Optional.of(logprobs);
      return this;
    }

    /**
     * @param topLogprobs An integer between 0 and 5 specifying the number of most likely tokens to
     *     return at each token position, each with an associated log probability. logprobs must be
     *     set to true if this parameter is used.
     */
    public Builder topLogprobs(int topLogprobs) {
      if (topLogprobs < 0 || topLogprobs > 5) {
        throw new IllegalArgumentException(
            "topLogprobs must be between 0 and 5 but it was " + topLogprobs);
      }
      this.topLogprobs = Optional.of(topLogprobs);
      return this;
    }

    /**
     * @param maxTokens The total length of input tokens and generated tokens is limited by the
     *     model's context length
     */
    public Builder maxTokens(int maxTokens) {
      if (maxTokens < 1) {
        throw new IllegalArgumentException("maxTokens must be a positive number");
      }
      this.maxTokens = Optional.of(maxTokens);
      return this;
    }

    /**
     * @param n How many chat completion choices to generate for each input message. Note that you
     *     will be charged based on the number of generated tokens across all of the choices. Keep n
     *     as 1 to minimize costs.
     */
    public Builder n(int n) {
      if (n < 1) {
        throw new IllegalArgumentException("n must be a positive number");
      }
      this.n = Optional.of(n);
      return this;
    }

    /**
     * @param presencePenalty Number between -2.0 and 2.0. Positive values penalize new tokens based
     *     on whether they appear in the text so far, increasing the model's likelihood to talk
     *     about new topics.
     */
    public Builder presencePenalty(double presencePenalty) {
      if (presencePenalty < -2 || presencePenalty > 2) {
        throw new IllegalArgumentException(
            "presencePenalty must be between -2.0 and 2.0 but it was " + presencePenalty);
      }
      this.presencePenalty = Optional.of(presencePenalty);
      return this;
    }

    /**
     * <b>Important:</b> when using JSON mode, you must also instruct the model to produce JSON
     * yourself via a system or user message.
     *
     * @param responseFormat An object specifying the format that the model must output.
     */
    public Builder responseFormat(ResponseFormat responseFormat) {
      this.responseFormat = Optional.of(responseFormat);
      return this;
    }

    /**
     * @param seed If specified, the system will make a best effort to sample deterministically,
     *     such that repeated requests with the same seed and parameters should return the same
     *     result. Determinism is not guaranteed, and you should refer to the system_fingerprint
     *     response parameter to monitor changes in the backend.
     */
    public Builder seed(int seed) {
      this.seed = Optional.of(seed);
      return this;
    }

    /**
     * @param stop Up to 4 sequences where the API will stop generating further tokens.
     */
    public Builder stop(String... stop) {
      if (stop.length > 4) {
        throw new IllegalArgumentException(
            "Up to 4 stop sequences could be defined, but it was " + stop.length);
      }
      this.stop.addAll(Arrays.asList(stop));
      return this;
    }

    /**
     * @param stream If set, partial message deltas will be sent, like in ChatGPT. Tokens will be
     *     sent as data-only server-sent events as they become available.
     */
    public Builder stream(boolean stream) {
      this.stream = Optional.of(stream);
      return this;
    }

    /**
     * @param temperature What sampling temperature to use, between 0 and 2. Higher values like 0.8
     *     will make the output more random, while lower values like 0.2 will make it more focused
     *     and deterministic.
     */
    public Builder temperature(double temperature) {
      if (temperature < 0 || temperature > 2) {
        throw new IllegalArgumentException(
            "temperature must be between 0 and 2 but it was " + temperature);
      }
      this.temperature = Optional.of(temperature);
      return this;
    }

    /**
     * @param topP An alternative to sampling with temperature, called nucleus sampling, where the
     *     model considers the results of the tokens with top_p probability mass. So 0.1 means only
     *     the tokens comprising the top 10% probability mass are considered.
     */
    public Builder topP(double topP) {
      this.topP = Optional.of(topP);
      return this;
    }

    /**
     * @param tool tool to append to the list of tools the model may call. Currently, only functions
     *     are supported as a tool. Use this to provide a list of functions the model may generate
     *     JSON inputs for.
     */
    public Builder tool(Tool tool) {
      tools.add(tool);
      return this;
    }

    /**
     * @param tools tools to append to the list of tools the model may call. Currently, only
     *     functions are supported as a tool. Use this to provide a list of functions the model may
     *     generate JSON inputs for.
     */
    public Builder tools(List<Tool> tools) {
      this.tools.addAll(tools);
      return this;
    }

    /**
     * @param toolChoice Controls which (if any) function is called by the model. none means the
     *     model will not call a function and instead generates a message. auto means the model can
     *     pick between generating a message or calling a function. Specifying a particular function
     *     via {"type": "function", "function": {"name": "my_function"}} forces the model to call
     *     that function.
     *     <p>none is the default when no functions are present. auto is the default if functions
     *     are present.
     */
    public Builder toolChoice(String toolChoice) {
      this.toolChoice = Optional.of(toolChoice);
      return this;
    }

    /**
     * @param toolChoice Controls which (if any) function is called by the model. none means the
     *     model will not call a function and instead generates a message. auto means the model can
     *     pick between generating a message or calling a function. Specifying a particular function
     *     via {"type": "function", "function": {"name": "my_function"}} forces the model to call
     *     that function.
     *     <p>none is the default when no functions are present. auto is the default if functions
     *     are present.
     */
    public Builder toolChoice(ToolChoice toolChoice) {
      this.toolChoice = Optional.of(toolChoice);
      return this;
    }

    /**
     * @param user A unique identifier representing your end-user, which can help OpenAI to monitor
     *     and detect abuse.
     */
    public Builder user(String user) {
      this.user = Optional.of(user);
      return this;
    }

    public ChatRequest build() {
      return new ChatRequest(
          model,
          List.copyOf(messages),
          frequencyPenalty,
          logitBias,
          logprobs,
          topLogprobs,
          maxTokens,
          n,
          presencePenalty,
          responseFormat,
          seed,
          stop.isEmpty() ? Optional.empty() : Optional.of(List.copyOf(stop)),
          stream,
          temperature,
          topP,
          tools.isEmpty() ? Optional.empty() : Optional.of(List.copyOf(tools)),
          toolChoice,
          user);
    }
  }
}