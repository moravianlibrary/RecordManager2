package cz.mzk.recordmanager.server.kramerius.fulltext;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.DedupRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public class KrameriusFulltextWriter implements ItemWriter<HarvestedRecord> {

	private static final Logger logger = LoggerFactory.getLogger(KrameriusFulltextWriter.class);

	@Autowired
	private HarvestedRecordDAO recordDao;

	@Autowired
	private DedupRecordDAO dedupDao;

	@Override
	public void write(List<? extends HarvestedRecord> items) throws Exception {
		try {
			for (HarvestedRecord hr : items) {
				if (!hr.getShouldBeProcessed()) continue;
				DedupRecord dr = hr.getDedupRecord();
				if(dr != null){
					dr.setUpdated(new Date());
					dedupDao.persist(dr);
				}
				recordDao.persist(hr);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

}