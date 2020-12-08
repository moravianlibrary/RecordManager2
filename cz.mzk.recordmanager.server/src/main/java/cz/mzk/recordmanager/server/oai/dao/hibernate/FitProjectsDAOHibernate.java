package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.FitProject;
import cz.mzk.recordmanager.server.model.FitProject.FitProjectEnum;
import cz.mzk.recordmanager.server.oai.dao.FitProjectDAO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class FitProjectsDAOHibernate extends AbstractDomainDAOHibernate<Long, FitProject>
		implements FitProjectDAO {

	@Override
	public List<FitProject> getProjectsFromEnums(List<FitProjectEnum> projects) {
		Set<FitProject> formatSet = new HashSet<>();
		for (FitProjectEnum fpe : projects) {
			formatSet.add(getProjectsFromEnums(fpe));
		}
		return new ArrayList<>(formatSet);
	}

	@Override
	public FitProject getProjectsFromEnums(FitProjectEnum project) {
		return get(project.getNumValue());
	}
}
