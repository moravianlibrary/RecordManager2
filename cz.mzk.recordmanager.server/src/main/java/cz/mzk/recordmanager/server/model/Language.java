package cz.mzk.recordmanager.server.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.google.common.base.Preconditions;

@Entity
@Table(name=Language.TABLE_NAME)
public class Language {
	
	public static final String TABLE_NAME = "language";
	
	@Embeddable
	protected static class LanguageId implements Serializable {

		private static final long serialVersionUID = 1L;

		@Column(name="harvested_record_id")
		private long harvestedRecordId;

		@Column(name="lang")
		private String langStr;

		// for hibernate
		protected LanguageId(){
		}
		
		public LanguageId(long harvestedRecordId, String langStr) {
			super();
			this.harvestedRecordId = harvestedRecordId;
			this.langStr = langStr;
		}

		public Long getHarvestedRecordId() {
			return harvestedRecordId;
		}

		public String getLangStr() {
			return langStr;
		}

		@Override
		public int hashCode() {
			return Objects.hash(harvestedRecordId, langStr);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			LanguageId other = (LanguageId) obj;
			return Objects.equals(this.getHarvestedRecordId(), other.getHarvestedRecordId()) &&
					Objects.equals(this.getLangStr(), other.getLangStr());
		}

	}

	@EmbeddedId
	private LanguageId id;

	protected Language(){
	}
	
	public Language(HarvestedRecord record, String language) {
		super();
		Preconditions.checkNotNull(record, "record");
		Preconditions.checkNotNull(language, "language");
		this.id = new LanguageId(record.getId(), language);
	}

	public String getLangStr() {
		return id.getLangStr();
	}

	@Override
	public String toString() {
		return "Language [langStr=" + id.getLangStr() + "]";
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		Language other = (Language) obj;
		return this.id.equals(other.id);
	}

}
