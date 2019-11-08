package cz.mzk.recordmanager.server.bibliolinker.keys;

import cz.mzk.recordmanager.server.marc.InvalidMarcException;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.ProgressLogger;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@StepScope
public class RegenerateBiblioLinkerKeysWriter implements ItemWriter<Long> {

	private static Logger logger = LoggerFactory.getLogger(RegenerateBiblioLinkerKeysWriter.class);

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	@Autowired
	protected DelegatingBiblioLinkerKeysParser biblioLinkerKeysParser;

	@Autowired
	protected SessionFactory sessionFactory;

	private ProgressLogger progressLogger = new ProgressLogger(logger, 10000);

	@Override
	public void write(List<? extends Long> ids) {
		for (Long id : ids) {
			HarvestedRecord rec = harvestedRecordDao.get(id);
			progressLogger.incrementAndLogProgress(rec);
			if (!rec.getHarvestedFrom().isGenerateBiblioLinkerKeys()
					|| rec.getRawRecord() == null || rec.getRawRecord().length == 0) {
				if (rec.getBiblioLinkerKeysHash() != null && !rec.getBiblioLinkerKeysHash().equals("")) {
					harvestedRecordDao.dropBilioLinkerKeys(rec);
					rec.setBiblioLinkerKeysHash(null);
					harvestedRecordDao.saveOrUpdate(rec);
				}
				continue;
			}
			try {
				String oldHash = rec.getBiblioLinkerKeysHash();
				rec = biblioLinkerKeysParser.parse(rec);
				if (rec.getBiblioLinkerKeysHash() != null && rec.getBiblioLinkerKeysHash().equals(oldHash)) continue;
				harvestedRecordDao.saveOrUpdate(rec);
			} catch (InvalidMarcException ime) {
				logger.warn("Invalid Marc in record: " + rec.getId());
			} catch (Exception e) {
				logger.warn("Skipping record due to error: " + e);
			}
		}
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
	}
}
