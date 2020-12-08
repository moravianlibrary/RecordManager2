package cz.mzk.recordmanager.server.oai.dao.hibernate;

import cz.mzk.recordmanager.server.model.FitKnowledgeBase;
import cz.mzk.recordmanager.server.oai.dao.FitKnowledgeBaseDAO;
import org.springframework.stereotype.Component;

@Component
public class FitKnowledgeBaseDAOHibernate extends AbstractDomainDAOHibernate<Long, FitKnowledgeBase>
		implements FitKnowledgeBaseDAO {

}
