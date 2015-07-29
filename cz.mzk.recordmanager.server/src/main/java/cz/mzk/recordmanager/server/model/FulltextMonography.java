package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name=FulltextMonography.TABLE_NAME)
public class FulltextMonography extends AbstractDomainObject {

	public static final String TABLE_NAME = "fulltext_monography";
	
	@Column(name="uuid_page")
	private String uuidPage;
	
	@Column(name="order_in_monography")
	private Long order;
	
	@Column(name="fulltext")
	private byte[] fulltext;

	public String getUuidPage() {
		return uuidPage;
	}

	public void setUuidPage(String uuidPage) {
		this.uuidPage = uuidPage;
	}

	public Long getOrder() {
		return order;
	}

	public void setOrder(Long order) {
		this.order = order;
	}

	public byte[] getFulltext() {
		return fulltext;
	}

	public void setFulltext(byte[] fulltext) {
		this.fulltext = fulltext;
	}
	
	
	
}
