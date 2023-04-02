package com.libre.framework.tookit.moudle.openapi.service;

import com.libre.framework.tookit.config.ChatGPTProperties;
import com.libre.framework.tookit.moudle.openapi.pojo.ChatRequest;
import com.libre.framework.tookit.moudle.openapi.pojo.ChatResponse;
import com.libre.toolkit.exception.LibreException;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.http.HttpRequest;
import okhttp3.RequestBody;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeTypeUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Libre
 * @Date: 2023/1/23 5:13 PM
 */
@Component
@RequiredArgsConstructor
public class ChatGPTServiceImpl implements ChatGPTService, SmartInitializingSingleton {

	private final Map<String, String> headers = new HashMap<>();

	private final ChatGPTProperties properties;

	@Override
	public String request(String prompt) {
		ChatRequest chatRequest = new ChatRequest(prompt);
		return request(chatRequest);
	}

	@Override
	public String request(ChatRequest request) {
		ChatResponse response = HttpRequest
				.post(properties.getUrl())
				.readTimeout(Duration.ofMinutes(2))
				.writeTimeout(Duration.ofMinutes(2))
				.addHeader(headers)
				.useSlf4jLog()
				.bodyJson(request)
				.execute()
				.asValue(ChatResponse.class);
		List<ChatResponse.ChoicesItem> choices = response.getChoices();
		if (CollectionUtils.isEmpty(choices)) {
			throw new LibreException("响应为空");
		}
		ChatResponse.ChoicesItem choicesItem = choices.get(0);
		return choicesItem.getText();
	}

	@Override
	public void afterSingletonsInstantiated() {
		headers.put("Authorization", properties.getToken());
		headers.put("Content-Type", MimeTypeUtils.APPLICATION_JSON_VALUE);
	}

}
