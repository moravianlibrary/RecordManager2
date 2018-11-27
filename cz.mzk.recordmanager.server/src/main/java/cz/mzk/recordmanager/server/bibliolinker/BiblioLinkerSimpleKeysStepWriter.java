package cz.mzk.recordmanager.server.bibliolinker;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.ProgressLogger;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class BiblioLinkerSimpleKeysStepWriter implements
		ItemWriter<List<HarvestedRecord>> {

	private static Logger logger = LoggerFactory.getLogger(BiblioLinkerSimpleKeysStepWriter.class);

	@Autowired
	private HarvestedRecordDAO harvestedRecordDAO;

	@Autowired
	protected SessionFactory sessionFactory;

	private ProgressLogger progressLogger = new ProgressLogger(logger, 1000);

	@Override
	public void write(List<? extends List<HarvestedRecord>> arg0)
			throws Exception {
		for (List<HarvestedRecord> hrList : arg0) {
			for (HarvestedRecord hr : hrList) {
				harvestedRecordDAO.saveOrUpdate(hr);
				progressLogger.incrementAndLogProgress();
			}
		}
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
	}

}
