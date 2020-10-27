package cz.mzk.recordmanager.server.miscellaneous.fit.semanticEnrichment;

import cz.mzk.recordmanager.server.model.FitKnowledgeBase;
import cz.mzk.recordmanager.server.oai.dao.FitKnowledgeBaseDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ImportKnowledgeBaseWriter implements ItemWriter<List<FitKnowledgeBase>> {

	private static Logger logger = LoggerFactory.getLogger(ImportKnowledgeBaseWriter.class);

	@Autowired
	private FitKnowledgeBaseDAO knowledgeBaseDAO;

	@Override
	public void write(List<? extends List<FitKnowledgeBase>> items) throws Exception {
		for (List<FitKnowledgeBase> item : items) {
			for (FitKnowledgeBase knowledgeBase : item) {
				knowledgeBaseDAO.saveOrUpdate(knowledgeBase);
			}
		}
	}
}
