package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.FitProject;
import cz.mzk.recordmanager.server.model.FitProject.FitProjectEnum;

import java.util.List;

public interface FitProjectDAO extends DomainDAO<Long, FitProject> {

	List<FitProject> getProjectsFromEnums(List<FitProjectEnum> projects);

	FitProject getProjectsFromEnums(FitProjectEnum project);
}
