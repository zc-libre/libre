package com.libre.framework.toolkit.moudle.openapi.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ChatResponse {

	private int created;

	private Usage usage;

	private String model;

	private String id;

	private List<ChoicesItem> choices;

	private String object;

	@Data
	public static class ChoicesItem {

		@JsonProperty("finish_reason")
		private String finishReason;

		private int index;

		private ResponseMessage message;

		private Object logprobs;

	}

	@Data
	public static class ResponseMessage {

		private String role;

		private String content;

	}

	@Data
	public static class Usage {

		@JsonProperty("completion_tokens")
		private Integer completionTokens;

		@JsonProperty("prompt_tokens")
		private Integer promptTokens;

		@JsonProperty("total_tokens")
		private Integer totalTokens;

	}

}