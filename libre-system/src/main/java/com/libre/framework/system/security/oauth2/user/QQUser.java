package com.libre.framework.system.security.oauth2.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;

public class QQUser extends DefaultOAuth2User {

	/**
	 * Constructs a {@code DefaultOAuth2User} using the provided parameters.
	 * @param authorities the authorities granted to the user
	 * @param attributes the attributes about the user
	 * @param nameAttributeKey the key used to access the user's &quot;name&quot; from
	 * {@link #getAttributes()}
	 */
	public QQUser(Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes,
			String nameAttributeKey) {
		super(authorities, attributes, nameAttributeKey);
	}

}
