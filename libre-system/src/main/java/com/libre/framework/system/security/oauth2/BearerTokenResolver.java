package com.libre.framework.system.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * spring-boot-starter-oauth2-resource-server 依赖包中官方提供 BearerToken相关操作，但 client端未提供，这里直接把
 * DefaultBearerTokenResolver搬运过来
 *
 * @author bty
 * @date 2023/2/6
 * @since 17
 **/
@Setter
@Component
public class BearerTokenResolver {

	private static final Pattern authorizationPattern = Pattern.compile("^Bearer (?<token>[a-zA-Z0-9-._~+/]+=*)$",
			Pattern.CASE_INSENSITIVE);

	/**
	 * -- SETTER -- Set if transport of access token using form-encoded body parameter is
	 * supported. Defaults to .
	 * @param allowFormEncodedBodyParameter if the form-encoded body parameter is
	 * supported
	 */
	private boolean allowFormEncodedBodyParameter = false;

	/**
	 * -- SETTER -- Set if transport of access token using URI query parameter is
	 * supported. Defaults to . The spec recommends against using this mechanism for
	 * sending bearer tokens, and even goes as far as stating that it was only included
	 * for completeness.
	 * @param allowUriQueryParameter if the URI query parameter is supported
	 */
	private boolean allowUriQueryParameter = false;

	/**
	 * -- SETTER -- Set this value to configure what header is checked when resolving a
	 * Bearer Token. This value is defaulted to . This allows other headers to be used as
	 * the Bearer Token source such as
	 * @param bearerTokenHeaderName the header to check when retrieving the Bearer Token.
	 *
	 */
	private String bearerTokenHeaderName = HttpHeaders.AUTHORIZATION;

	public String resolve(final HttpServletRequest request) {
		final String authorizationHeaderToken = resolveFromAuthorizationHeader(request);
		final String parameterToken = isParameterTokenSupportedForRequest(request)
				? resolveFromRequestParameters(request) : null;
		if (authorizationHeaderToken != null) {
			if (parameterToken != null) {
				throw new OAuth2AuthenticationException("Found multiple bearer tokens in the request");
			}
			return authorizationHeaderToken;
		}
		if (parameterToken != null && isParameterTokenEnabledForRequest(request)) {
			return parameterToken;
		}
		return null;
	}

	private String resolveFromAuthorizationHeader(HttpServletRequest request) {
		String authorization = request.getHeader(this.bearerTokenHeaderName);
		if (!StringUtils.startsWithIgnoreCase(authorization, "bearer")) {
			return null;
		}
		Matcher matcher = authorizationPattern.matcher(authorization);
		if (!matcher.matches()) {
			throw new OAuth2AuthenticationException("Bearer token is malformed");
		}
		return matcher.group("token");
	}

	private static String resolveFromRequestParameters(HttpServletRequest request) {
		String[] values = request.getParameterValues("access_token");
		if (values == null || values.length == 0) {
			return null;
		}
		if (values.length == 1) {
			return values[0];
		}
		throw new OAuth2AuthenticationException("Found multiple bearer tokens in the request");
	}

	private boolean isParameterTokenSupportedForRequest(final HttpServletRequest request) {
		return (("POST".equals(request.getMethod())
				&& MediaType.APPLICATION_FORM_URLENCODED_VALUE.equals(request.getContentType()))
				|| "GET".equals(request.getMethod()));
	}

	private boolean isParameterTokenEnabledForRequest(final HttpServletRequest request) {
		return ((this.allowFormEncodedBodyParameter && "POST".equals(request.getMethod())
				&& MediaType.APPLICATION_FORM_URLENCODED_VALUE.equals(request.getContentType()))
				|| (this.allowUriQueryParameter && "GET".equals(request.getMethod())));
	}

}
