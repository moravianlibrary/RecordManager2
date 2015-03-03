package cz.mzk.recordmanager.server.dedup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;

public class UpdateHarvestedRecordProcessor implements ItemProcessor<Long, HarvestedRecord> {
	
	private static Logger logger = LoggerFactory.getLogger(UpdateHarvestedRecordProcessor.class);

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	@Autowired
	protected DelegatingDedupKeysParser dedupKeysParser;

	@Override
	public HarvestedRecord process(Long id) throws Exception {
		if (harvestedRecordDao == null) {
			throw new NullPointerException("harvestedRecordDao");
		}
		logger.debug("About to fetch harvested record with id={}", id);
		HarvestedRecord record = harvestedRecordDao.get(id);
		dedupKeysParser.parse(record);
		return record;
	}

}
