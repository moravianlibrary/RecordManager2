package cz.mzk.recordmanager.webapp;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class ShibbolethAuthenticationToken extends AbstractAuthenticationToken {

	private static final long serialVersionUID = 1L;

	private final String eppn;

	public ShibbolethAuthenticationToken(String eppn,
			Collection<? extends GrantedAuthority> authorities) {
		super(authorities);
		this.eppn = eppn;
	}

	@Override
	public Object getCredentials() {
		return null;
	}

	@Override
	public Object getPrincipal() {
		return this.eppn;
	}

}
