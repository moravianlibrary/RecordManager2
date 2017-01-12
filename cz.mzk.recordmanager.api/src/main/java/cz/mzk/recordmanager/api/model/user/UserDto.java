package cz.mzk.recordmanager.api.model.user;


public abstract class UserDto {
	private String login;

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}
}
