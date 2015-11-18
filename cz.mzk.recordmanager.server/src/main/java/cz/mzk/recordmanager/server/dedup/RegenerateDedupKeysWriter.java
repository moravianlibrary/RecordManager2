package cz.mzk.recordmanager.server.dedup;

import java.util.Calendar;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.marc.InvalidMarcException;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;

@Component
@StepScope
public class RegenerateDedupKeysWriter implements ItemWriter<Long> {

	private static Logger logger = LoggerFactory.getLogger(RegenerateDedupKeysWriter.class);
	
	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;
	
	@Autowired
	protected DelegatingDedupKeysParser dedupKeysParser;
	
	private static final int LOG_PERIOD = 10000;
	private int totalCount  = 0;
	private long startTime = 0L;
	
	@PostConstruct
	public void initialize() {
		startTime = Calendar.getInstance().getTimeInMillis();
	}
	
	@Override
	public void write(List<? extends Long> ids) {
		for (Long id : ids) {
			HarvestedRecord rec = harvestedRecordDao.get(id);	
			if (rec.getDeleted() != null) {
				continue;
			}
			try {
				rec = dedupKeysParser.parse(rec);
				harvestedRecordDao.persist(rec);
				++totalCount;
				logProgress();
			} catch (InvalidMarcException ime) {
				logger.warn("Invalid Marc in record: " + rec.getId());
			} catch (Exception e) {
				logger.warn("Skipping record due to error: " + e.toString());
			}

			
		}
	}
	
	protected void logProgress() {
		if (totalCount % LOG_PERIOD == 0) {
			long elapsedSecs = (Calendar.getInstance().getTimeInMillis() - startTime) / 1000;
			if (elapsedSecs == 0) elapsedSecs = 1;
			logger.info(String.format("Regenerated keys: %,9d, processing speed %4d records/s",
							totalCount, totalCount / elapsedSecs));
		}
	}
}
