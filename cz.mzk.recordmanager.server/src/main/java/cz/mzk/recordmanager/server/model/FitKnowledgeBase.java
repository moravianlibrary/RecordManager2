package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = FitKnowledgeBase.TABLE_NAME)
public class FitKnowledgeBase {

	public static final String TABLE_NAME = "fit_knowledge_base";

	@Id
	@Column(name = "id")
	private Long id;

	@Column(name = "data")
	private String data;

	public static FitKnowledgeBase create(Long id, String data) {
		FitKnowledgeBase knowledgeBase = new FitKnowledgeBase();
		knowledgeBase.setId(id);
		knowledgeBase.setData(data);
		return knowledgeBase;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "FitKnowledgeBase{" +
				"id=" + id +
				", data='" + data + '\'' +
				'}';
	}
}