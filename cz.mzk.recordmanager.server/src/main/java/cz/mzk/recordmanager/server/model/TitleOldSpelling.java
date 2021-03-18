package cz.mzk.recordmanager.server.model;

import javax.persistence.*;

@Entity
@Table(name = TitleOldSpelling.TABLE_NAME)
public class TitleOldSpelling {

	public static final String TABLE_NAME = "title_old_spelling";

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "key")
	private String key;

	@Column(name = "value")
	private String value;

	public static TitleOldSpelling create(String key, String value) {
		TitleOldSpelling titleOldSpelling = new TitleOldSpelling();
		titleOldSpelling.setKey(key);
		titleOldSpelling.setValue(value);
		return titleOldSpelling;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "TitleOldSpelling{" +
				"id=" + id +
				", key='" + key + '\'' +
				", value='" + value + '\'' +
				'}';
	}
}