package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = Classifier.TABLE_NAME)
public class Classifier extends AbstractDomainObject {

	public static final String TABLE_NAME = "classifier";

	@Column(name = "value")
	private String value;

	@Column(name = "relevance")
	private String relevance;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getRelevance() {
		return relevance;
	}

	public void setRelevance(String relevance) {
		this.relevance = relevance;
	}

	@Override
	public String toString() {
		return "Classifier{" +
				"value='" + value + '\'' +
				", relevance='" + relevance + '\'' +
				'}';
	}
}
