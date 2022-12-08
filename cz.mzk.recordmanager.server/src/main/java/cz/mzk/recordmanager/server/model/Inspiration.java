package cz.mzk.recordmanager.server.model;

import cz.mzk.recordmanager.server.imports.inspirations.InspirationType;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@Table(name = Inspiration.TABLE_NAME)
public class Inspiration {

	public static final String TABLE_NAME = "inspiration";

	public Inspiration() {
	}

	public static Inspiration create(String name, InspirationType type) {
		Inspiration newInspiration = new Inspiration();
		newInspiration.setName(name);
		newInspiration.setType(type);
		return newInspiration;
	}

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name")
	private String name;

	@Type(
			type = "cz.mzk.recordmanager.server.hibernate.StringEnumUserType",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "enumClassName", value = "cz.mzk.recordmanager.server.imports.inspirations.InspirationType"),
			}
	)
	@Column(name = "type")
	private InspirationType type;

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

	public InspirationType getType() {
		return type;
	}

	public void setType(InspirationType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Inspiration{" +
				"name='" + name + '\'' +
				", type='" + type + '\'' +
				'}';
	}

}
