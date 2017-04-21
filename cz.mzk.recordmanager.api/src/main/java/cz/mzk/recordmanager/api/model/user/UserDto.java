package cz.mzk.recordmanager.api.model.user;

import java.io.Serializable;
import java.util.List;

public class UserDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String login;

	private final List<String> roles;

	public UserDto(String login, List<String> roles) {
		super();
		this.login = login;
		this.roles = roles;
	}

	public String getLogin() {
		return login;
	}

	public List<String> getRoles() {
		return roles;
	}

}
