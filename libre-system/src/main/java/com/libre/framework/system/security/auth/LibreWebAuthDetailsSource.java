package com.libre.framework.system.security.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

/**
 * 授权详情 添加验证码
 *
 * @author L.cm
 */
@Configuration(proxyBeanMethods = false)
public class LibreWebAuthDetailsSource
		implements AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails> {

	@Override
	public WebAuthenticationDetails buildDetails(HttpServletRequest context) {
		return new LibreWebAuthenticationDetails(context);
	}

}
