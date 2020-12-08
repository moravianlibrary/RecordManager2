package cz.mzk.recordmanager.server.miscellaneous.fit.semanticEnrichment;

import java.util.Objects;

public class SemanticEnrichment {

	private String recordId;
	private String type;
	private Long kbId;

	public static SemanticEnrichment create(String recordId, String type, Long kbId) {
		SemanticEnrichment item = new SemanticEnrichment();
		item.setRecordId(recordId);
		item.setType(type);
		item.setKbId(kbId);
		return item;
	}

	public String getRecordId() {
		return recordId;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Long getKbId() {
		return kbId;
	}

	public void setKbId(Long kbId) {
		this.kbId = kbId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SemanticEnrichment that = (SemanticEnrichment) o;
		return kbId.equals(that.kbId) &&
				recordId.equals(that.recordId) &&
				type.equals(that.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(recordId, type, kbId);
	}

	@Override
	public String toString() {
		return "SemanticEnrichment{" +
				"recordId='" + recordId + '\'' +
				", type='" + type + '\'' +
				", kbId=" + kbId +
				'}';
	}
}
