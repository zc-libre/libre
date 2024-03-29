package com.libre.framework.toolkit.moudle.openapi.controller;

import com.libre.framework.toolkit.moudle.openapi.service.ChatGPTService;
import com.libre.toolkit.result.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

/**
 * @author: Libre
 * @Date: 2023/1/23 5:33 PM
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/openapi")
public class OpenApiController {

	private final ChatGPTService chatGPTService;

	@PostMapping("/chat")
	public R<String> request(String question) throws ExecutionException, InterruptedException {
		String response = chatGPTService.request(question);
		return R.data(response);
	}

}
