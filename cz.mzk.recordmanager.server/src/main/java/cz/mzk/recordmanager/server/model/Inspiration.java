package cz.mzk.recordmanager.server.model;

import javax.persistence.*;

@Entity
@Table(name=Inspiration.TABLE_NAME)
public class Inspiration{
	
	public static final String TABLE_NAME = "inspiration";
	
	public Inspiration(){
	}
	
	public Inspiration(String name){
		setName(name);
	}

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name="harvested_record_id")
	private Long harvestedRecordId;
	
	@Column(name="name")
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getHarvestedRecordId() {
		return harvestedRecordId;
	}
	
	public void setHarvestedRecordId(Long id) {
		this.harvestedRecordId = id;
	}

	@Override
	public String toString() {
		return "Inspiration [harvestedRecordId=" + harvestedRecordId
				+ ", name=" + name + ']';
	}
	
}
