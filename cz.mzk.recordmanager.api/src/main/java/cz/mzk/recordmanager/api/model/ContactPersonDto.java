package cz.mzk.recordmanager.api.model;

import java.io.Serializable;

public class ContactPersonDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long id;

	private String name;
	
	private String email;
	
	private String phone;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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
