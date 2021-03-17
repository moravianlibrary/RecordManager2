package cz.mzk.recordmanager.server.model;

import javax.persistence.*;

@Entity
@Table(name = Pravopis.TABLE_NAME)
public class Pravopis {

	public static final String TABLE_NAME = "pravopis";

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "key")
	private String key;

	@Column(name = "value")
	private String value;

	public static Pravopis create(String key, String value) {
		Pravopis pravopis = new Pravopis();
		pravopis.setKey(key);
		pravopis.setValue(value);
		return pravopis;
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
		return "Pravopis{" +
				"id=" + id +
				", key='" + key + '\'' +
				", value='" + value + '\'' +
				'}';
	}
}