package cz.mzk.recordmanager.server.imports.inspirations;

import cz.mzk.recordmanager.server.model.Inspiration;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordInspirationDAO;
import cz.mzk.recordmanager.server.oai.dao.InspirationDAO;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Component
@StepScope
public class InspirationImportWriter implements ItemWriter<Map<String, List<String>>> {

	private static final Logger logger = LoggerFactory.getLogger(InspirationImportWriter.class);

	@Autowired
	protected SessionFactory sessionFactory;

	@Autowired
	private HarvestedRecordInspirationDAO harvestedRecordInspirationDao;

	@Autowired
	private InspirationDAO inspirationDAO;

	public InspirationImportWriter() {
	}

	private static final String TEXT_INFO = "Importing inspiration '%s' with %s records";

	@Override
	public void write(List<? extends Map<String, List<String>>> items)
			throws Exception {
		for (Map<String, List<String>> map : items) {
			for (Entry<String, List<String>> entry : map.entrySet()) {
				Inspiration inspiration;
				try {
					inspiration = inspirationDAO.getOrCreate(entry.getKey(), InspirationType.INSPIRATION);
				} catch (IOException e) {
					logger.error(e.getMessage());
					continue;
				}
				logger.info(String.format(TEXT_INFO, inspiration.getName(), entry.getValue().size()));
				for (String recordId : entry.getValue()) {
					String[] splitedId = recordId.split("\\.");
					if (splitedId.length != 2) continue;
					harvestedRecordInspirationDao.updateOrCreate(splitedId[0].toLowerCase(), splitedId[1], inspiration);
				}
				sessionFactory.getCurrentSession().flush();
				sessionFactory.getCurrentSession().clear();
			}
		}
	}
}
