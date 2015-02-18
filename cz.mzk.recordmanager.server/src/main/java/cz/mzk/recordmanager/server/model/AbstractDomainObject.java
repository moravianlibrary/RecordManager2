package cz.mzk.recordmanager.server.model;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.TableGenerator;

@MappedSuperclass
public class AbstractDomainObject {

	@Id
	@GeneratedValue(generator="recordmanager_key_gen", strategy = GenerationType.TABLE)
	@TableGenerator(
			name="recordmanager_key_gen", //
			pkColumnName="name", //
			valueColumnName="val", //
			pkColumnValue="recordmanager", // 
			initialValue=10000, //
			allocationSize=10, //
			table="recordmanager_key" //
	)
	@Column(name = "id", columnDefinition="decimal")
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		AbstractDomainObject other = (AbstractDomainObject) obj;
		return Objects.equals(this.getId(), other.getId());
	}

}
