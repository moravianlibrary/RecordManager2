package cz.mzk.recordmanager.server.dedup;

import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;

@Component
@StepScope
public class RegenerateDedupKeysWriter implements ItemWriter<HarvestedRecordUniqueId> {

	private static Logger logger = LoggerFactory.getLogger(RegenerateDedupKeysWriter.class);
	
	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;
	
	@Autowired
	protected DelegatingDedupKeysParser dedupKeysParser;
	
	private static final int LOG_PERIOD = 1000;
	private int totalCount  = 0;
	private long startTime = 0L;
	
	@Override
	public void write(List<? extends HarvestedRecordUniqueId> ids) throws Exception {
		for (HarvestedRecordUniqueId id : ids) {
			HarvestedRecord rec = harvestedRecordDao.get(id);
			dedupKeysParser.parse(rec);
			harvestedRecordDao.persist(rec);
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
