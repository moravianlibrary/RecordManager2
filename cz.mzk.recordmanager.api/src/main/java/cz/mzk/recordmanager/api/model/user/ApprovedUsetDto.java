package cz.mzk.recordmanager.api.model.user;


public class ApprovedUsetDto extends UserDto {

	private String token;


	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
