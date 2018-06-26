package cz.mzk.recordmanager.server.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.google.common.base.Preconditions;

@Entity
@Table(name=Sigla.TABLE_NAME)
public class Sigla extends AbstractDomainObject{

	public static final String TABLE_NAME = "sigla";
	
	@Embeddable
	public static class SiglaUniqueId implements Serializable{

		private static final long serialVersionUID = 1L;

		@Column(name="sigla")
		private String sigla;
		
		@Column(name="import_conf_id")
		private Long importConfId;
		
		public SiglaUniqueId(){
		}
		
		public SiglaUniqueId(ImportConfiguration importConf, String sigla) {
			super();
			Preconditions.checkNotNull(importConf, "importConf");
			Preconditions.checkNotNull(sigla, "sigla");
			this.importConfId = importConf.getId();
			this.sigla = sigla;
		}
		
		public SiglaUniqueId(Long importConfId, String sigla) {
			super();
			Preconditions.checkNotNull(importConfId, "importConf");
			Preconditions.checkNotNull(sigla, "sigla");
			this.importConfId = importConfId;
			this.sigla = sigla;
		}

		public String getSigla() {
			return sigla;
		}

		public Long getImportConfId() {
			return importConfId;
		}

		@Override
		public String toString() {
			return "SiglaUniqueId [sigla=" + sigla + ", importConfId="
					+ importConfId + ']';
		}
	}
	
	private SiglaUniqueId uniqueId;

	public SiglaUniqueId getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(SiglaUniqueId uniqueId) {
		this.uniqueId = uniqueId;
	}

	@Override
	public String toString() {
		return "Sigla [uniqueId=" + uniqueId + ']';
	}

}
