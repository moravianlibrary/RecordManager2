package cz.mzk.recordmanager.api.model.user;


public class LoginUserDto extends UserDto{

	private String password;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
