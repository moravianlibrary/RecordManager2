package cz.mzk.recordmanager.api.model;

import java.io.Serializable;

public class ContactPersonDto extends IdDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	private String email;

	private String phone;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}	

}
