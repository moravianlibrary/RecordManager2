package cz.mzk.recordmanager.webapp.controller;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import cz.mzk.recordmanager.api.model.user.UserDto;
import cz.mzk.recordmanager.webapp.ShibbolethAuthenticationToken;

@RestController
@RequestMapping(value = "/login")
public class AuthController {

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public UserDto login() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !(auth instanceof ShibbolethAuthenticationToken)) {
			return null;
		}
		ShibbolethAuthenticationToken shibToken = (ShibbolethAuthenticationToken) auth;
		Collection<GrantedAuthority> grantedAuthorities = shibToken.getAuthorities(); 
		List<String> authorities = grantedAuthorities.stream().map(it -> it.getAuthority()).collect(Collectors.toList());
		UserDto user = new UserDto(shibToken.getName(), authorities);
		return user;
	}

}

