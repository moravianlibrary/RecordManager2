package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name=Format.TABLE_NAME)
public class Format {
	
	public static final String TABLE_NAME = "format";

	@Id
	@Column(name="format")
	private String format;

	@Column(name="description")
	private String description;

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
