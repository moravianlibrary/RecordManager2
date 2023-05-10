package cz.mzk.recordmanager.server.model;

import javax.persistence.*;

@Entity
@Table(name = SiglaAll.TABLE_NAME)
public class SiglaAll extends AbstractDomainObject {

	public static final String TABLE_NAME = "sigla_all";

	@Column(name = "sigla")
	private String sigla;

	@Column(name = "import_conf_id")
	private Long harvestedFromId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "import_conf_id", insertable = false, updatable = false)
	private ImportConfiguration harvestedFrom;

	@Column(name = "cpk")
	private boolean cpk;

	@Column(name = "ziskej")
	private boolean ziskej;

	@Column(name = "ziskej_mvs_sigla")
	private String ziskejMvsSigla;

	@Column(name = "ziskej_edd")
	private boolean ziskej_edd;

	@Column(name = "ziskej_edd_sigla")
	private String ziskejEddSigla;

	@Column(name = "dnnt")
	private boolean dnnt;

	public String getSigla() {
		return sigla;
	}

	public void setSigla(String sigla) {
		this.sigla = sigla;
	}

	public Long getHarvestedFromId() {
		return harvestedFromId;
	}

	public ImportConfiguration getHarvestedFrom() {
		return harvestedFrom;
	}

	public void setHarvestedFrom(ImportConfiguration harvestedFrom) {
		this.harvestedFrom = harvestedFrom;
	}

	public void setHarvestedFromId(Long harvestedFrom) {
		this.harvestedFromId = harvestedFrom;
	}

	public boolean isZiskej() {
		return ziskej;
	}

	public void setZiskej(boolean ziskej) {
		this.ziskej = ziskej;
	}

	public boolean isDnnt() {
		return dnnt;
	}

	public void setDnnt(boolean dnnt) {
		this.dnnt = dnnt;
	}

	public boolean isCpk() {
		return cpk;
	}

	public void setCpk(boolean cpk) {
		this.cpk = cpk;
	}

	public boolean isZiskej_edd() {
		return ziskej_edd;
	}

	public void setZiskej_edd(boolean ziskej_edd) {
		this.ziskej_edd = ziskej_edd;
	}

	@Override
	public String toString() {
		return "SiglaAll{" +
				"sigla='" + sigla + '\'' +
				", harvestedFromId=" + harvestedFromId +
				", harvestedFrom=" + harvestedFrom +
				", cpk=" + cpk +
				", ziskej=" + ziskej +
				", ziskej_edd=" + ziskej_edd +
				", dnnt=" + dnnt +
				'}';
	}

}
