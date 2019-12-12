package cz.mzk.recordmanager.server.dedup;

import cz.mzk.recordmanager.server.marc.InvalidMarcException;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.ProgressLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@StepScope
public class RegenerateDedupKeysWriter implements ItemWriter<Long> {

	private static Logger logger = LoggerFactory.getLogger(RegenerateDedupKeysWriter.class);
	
	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;
	
	@Autowired
	protected DelegatingDedupKeysParser dedupKeysParser;

	private ProgressLogger progressLogger = new ProgressLogger(logger, 10000);

	@Override
	public void write(List<? extends Long> ids) {
		for (Long id : ids) {
			HarvestedRecord rec = harvestedRecordDao.get(id);

			progressLogger.incrementAndLogProgress();
			if (!rec.getHarvestedFrom().isGenerateDedupKeys()
					|| rec.getRawRecord() == null || rec.getRawRecord().length == 0) {
				if (rec.getDedupKeysHash() != null && !rec.getDedupKeysHash().equals("")) {
					harvestedRecordDao.dropDedupKeys(rec);
					rec.setDedupKeysHash("");
					harvestedRecordDao.persist(rec);
				}
				continue;
			}
			try {
				String oldHash = rec.getDedupKeysHash();
				rec = dedupKeysParser.parse(rec);
				if (rec.getDedupKeysHash() != null && rec.getDedupKeysHash().equals(oldHash)) continue;
				harvestedRecordDao.persist(rec);
			} catch (InvalidMarcException ime) {
				logger.warn("Invalid Marc in record: " + rec.getId());
			} catch (Exception e) {
				logger.warn("Skipping record due to error: " + e);
			}

		}
	}
}
