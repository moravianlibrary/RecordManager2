package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = FitProject.TABLE_NAME)
public class FitProject {

	public static final String TABLE_NAME = "fit_project";

	public enum FitProjectEnum {
		FULLTEXT_ANALYSER(1L),
		SEMANTIC_ENRICHMENT(2L),
		CLASSIFIER(3L);

		private final Long numValue;

		FitProjectEnum(Long numValue) {
			this.numValue = numValue;
		}

		public Long getNumValue() {
			return numValue;
		}

	}

	@Id
	@Column(name = "id")
	private Long id;

	@Column(name = "name")
	private String name;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "FitProject{" +
				"id=" + id +
				", name='" + name + '\'' +
				'}';
	}
}