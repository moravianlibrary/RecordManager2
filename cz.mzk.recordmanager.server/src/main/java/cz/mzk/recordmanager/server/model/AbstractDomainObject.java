package cz.mzk.recordmanager.server.model;

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

}
