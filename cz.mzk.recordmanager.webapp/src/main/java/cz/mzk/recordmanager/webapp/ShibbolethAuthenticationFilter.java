package cz.mzk.recordmanager.webapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class ShibbolethAuthenticationFilter extends OncePerRequestFilter implements Filter {

	private static Logger logger = LoggerFactory
			.getLogger(ShibbolethAuthenticationFilter.class);

	private final GrantedAuthority ADMIN_AUTHORITY = new SimpleGrantedAuthority("ADMIN");

	private final List<String> admins;

	public ShibbolethAuthenticationFilter(List<String> admins) {
		this.admins = admins;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String eppn = request.getHeader("eduPersonPrincipalName");
		if (eppn != null) {
			List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
			if (admins.contains(eppn)) {
				authorities.add(ADMIN_AUTHORITY);
			}
			ShibbolethAuthenticationToken auth = new ShibbolethAuthenticationToken(eppn, authorities);
			SecurityContextHolder.getContext().setAuthentication(auth);
		}
        filterChain.doFilter(request, response);
	}

}