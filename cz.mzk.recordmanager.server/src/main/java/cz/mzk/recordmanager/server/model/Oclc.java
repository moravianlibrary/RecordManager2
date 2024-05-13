package cz.mzk.recordmanager.server.model;

import cz.mzk.recordmanager.server.util.MetadataUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name=Oclc.TABLE_NAME)
public class Oclc extends AbstractDomainObject {
	
	public static final String TABLE_NAME = "oclc";

	@Column(name = "harvested_record_id", updatable = false, insertable = false)
	private Long harvestedRecordId;

	@Column(name="oclc")
	public String oclcStr;

	public String getOclcStr() {
		return oclcStr;
	}

	public void setOclcStr(String oclcStr) {
		this.oclcStr = MetadataUtils.shorten(oclcStr, 20);
	}
	
	
}
