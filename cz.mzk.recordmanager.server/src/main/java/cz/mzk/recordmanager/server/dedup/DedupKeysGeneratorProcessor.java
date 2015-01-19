package cz.mzk.recordmanager.server.dedup;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;

public class DedupKeysGeneratorProcessor implements ItemProcessor<Long, HarvestedRecord> {

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;
	
	@Override
	public HarvestedRecord process(Long id) throws Exception {
		return harvestedRecordDao.get(id);
	}

}
