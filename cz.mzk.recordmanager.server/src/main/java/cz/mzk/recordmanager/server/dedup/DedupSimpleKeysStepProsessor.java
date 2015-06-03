package cz.mzk.recordmanager.server.dedup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.DedupRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;

public class DedupSimpleKeysStepProsessor implements
		ItemProcessor<List<Long>, List<HarvestedRecord>> {

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;
	
	@Autowired
	private DedupRecordDAO dedupRecordDAO;
	
	private static Logger logger = LoggerFactory.getLogger(DedupSimpleKeysStepProsessor.class);
	
	@Override
	public List<HarvestedRecord> process(List<Long> idList) throws Exception {
		List<HarvestedRecord> hrList = new ArrayList<>();
		DedupRecord commonDedupRecord = null;
		for(Long id: idList) {
			HarvestedRecord currentHr = harvestedRecordDao.get(id);
			if (currentHr == null) {
				logger.warn("Missing record with id: " + id);
				continue;
			} 
			if (currentHr.getDedupRecord() != null) {
				commonDedupRecord = currentHr.getDedupRecord();
			}
			hrList.add(currentHr);
		}
		
		if (commonDedupRecord == null) {
			commonDedupRecord = new DedupRecord();
			commonDedupRecord.setUpdated(new Date());
		}
		
		commonDedupRecord = dedupRecordDAO.persist(commonDedupRecord);
		
		for (HarvestedRecord hr: hrList) {
			hr.setDedupRecord(commonDedupRecord);
		}
		
		return hrList;
	}

}
