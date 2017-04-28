package cz.mzk.recordmanager.webapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class ShibbolethAuthenticationProvider implements AuthenticationProvider {

	private static Logger logger = LoggerFactory
			.getLogger(ShibbolethAuthenticationProvider.class);

	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		return authentication;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return (ShibbolethAuthenticationToken.class.isAssignableFrom(authentication));
	}

}
