package cz.mzk.recordmanager.server.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = HarvestedRecordFitProject.TABLE_NAME)
public class HarvestedRecordFitProject {

	public static final String TABLE_NAME = "fit_project_link";

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "harvested_record_id", updatable = false, insertable = false)
	private Long harvestedRecordId;

	@Column(name = "fit_project_id", updatable = false, insertable = false)
	private Long fitProjectId;

	@Column(name = "fit_knowledge_base_id", updatable = false, insertable = false)
	private Long fitKnowledgeBaseId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "harvested_record_id")
	private HarvestedRecord harvestedRecord;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fit_project_id")
	private FitProject fitProject;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fit_knowledge_base_id")
	private FitKnowledgeBase fitKnowledgeBase;

	@Column(name = "data")
	private String data;

	public static HarvestedRecordFitProject create(FitProject project, String data) {
		return create(project, null, data);
	}

	public static HarvestedRecordFitProject create(FitProject project, FitKnowledgeBase kb, String data) {
		HarvestedRecordFitProject item = new HarvestedRecordFitProject();
		item.setData(data);
		if (project == null) throw new RuntimeException("No project selected!");
		item.setFitProject(project);
		item.setFitKnowledgeBase(kb);
		return item;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public FitProject getFitProject() {
		return fitProject;
	}

	public void setFitProject(FitProject fitProject) {
		this.fitProject = fitProject;
	}

	public FitKnowledgeBase getFitKnowledgeBase() {
		return fitKnowledgeBase;
	}

	public void setFitKnowledgeBase(FitKnowledgeBase fitKnowledgeBase) {
		this.fitKnowledgeBase = fitKnowledgeBase;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		HarvestedRecordFitProject that = (HarvestedRecordFitProject) o;
		return fitProject.equals(that.fitProject) &&
				Objects.equals(fitKnowledgeBase, that.fitKnowledgeBase) &&
				Objects.equals(data, that.data);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fitProject.getId(), fitKnowledgeBase, data);
	}

	@Override
	public String toString() {
		return "HarvestedRecordFitProject{" +
				"id=" + id +
				", harvestedRecordId=" + harvestedRecordId +
				", fitProjectId=" + fitProjectId +
				", harvestedRecord=" + harvestedRecord +
				", fitProject=" + fitProject +
				", data='" + data + '\'' +
				'}';
	}

}