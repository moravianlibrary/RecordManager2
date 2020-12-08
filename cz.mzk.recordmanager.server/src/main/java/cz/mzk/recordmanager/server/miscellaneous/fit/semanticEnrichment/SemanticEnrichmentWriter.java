package cz.mzk.recordmanager.server.miscellaneous.fit.semanticEnrichment;

import cz.mzk.recordmanager.server.model.FitKnowledgeBase;
import cz.mzk.recordmanager.server.model.FitProject.FitProjectEnum;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFitProject;
import cz.mzk.recordmanager.server.oai.dao.FitKnowledgeBaseDAO;
import cz.mzk.recordmanager.server.oai.dao.FitProjectDAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.ProgressLogger;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

public class SemanticEnrichmentWriter implements ItemWriter<List<SemanticEnrichment>> {

	@Autowired
	private HarvestedRecordDAO hrDao;

	@Autowired
	private FitProjectDAO fitProjectDAO;

	@Autowired
	private FitKnowledgeBaseDAO knowledgeBaseDAO;

	@Autowired
	protected SessionFactory sessionFactory;

	private static final Logger logger = LoggerFactory.getLogger(SemanticEnrichmentWriter.class);
	private final ProgressLogger progressLogger = new ProgressLogger(logger);

	@Override
	public void write(List<? extends List<SemanticEnrichment>> items) throws Exception {
		try {
			writeInner(items);
		} finally {
			sessionFactory.getCurrentSession().flush();
			sessionFactory.getCurrentSession().clear();
		}
	}

	protected void writeInner(List<? extends List<SemanticEnrichment>> items) {
		for (List<SemanticEnrichment> item : items) {
			progressLogger.incrementAndLogProgress();
			if (item.isEmpty()) continue;
			HarvestedRecord hr = hrDao.findByIdAndHarvestConfiguration(item.get(0).getRecordId(), Constants.IMPORT_CONF_ID_KRAM_MZK);
			if (hr == null) continue;
			for (SemanticEnrichment semanticEnrichment : item) {
				Set<HarvestedRecordFitProject> results = hr.getFitProjects();
				FitKnowledgeBase knowledgeBase = knowledgeBaseDAO.get(semanticEnrichment.getKbId());
				if (knowledgeBase == null) continue;
				results.add(HarvestedRecordFitProject.create(fitProjectDAO.getProjectsFromEnums(FitProjectEnum.SEMANTIC_ENRICHMENT),
						knowledgeBase, null));
				hr.setFitProjects(results);
			}
			hrDao.saveOrUpdate(hr);
		}
	}
}
