package cz.mzk.recordmanager.server.dedup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;

public class DedupKeysGeneratorProcessor implements ItemProcessor<Long, HarvestedRecord> {
	
	private static Logger logger = LoggerFactory.getLogger(DedupKeysGeneratorProcessor.class);

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;
	
	@Override
	public HarvestedRecord process(Long id) throws Exception {
		logger.debug("About to fetch harvested record with id={}", id);
		return harvestedRecordDao.get(id);
	}

}
