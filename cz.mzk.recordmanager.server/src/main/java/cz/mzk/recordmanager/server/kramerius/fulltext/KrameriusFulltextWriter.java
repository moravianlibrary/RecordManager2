package cz.mzk.recordmanager.server.kramerius.fulltext;

import java.util.Date;
import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.DedupRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;

public class KrameriusFulltextWriter implements ItemWriter<HarvestedRecord> {

	@Autowired
	private HarvestedRecordDAO recordDao;

	@Autowired
	private DedupRecordDAO dedupDao;
	
	@Autowired
	private HibernateSessionSynchronizer sync;

	@Override
	public void write(List<? extends HarvestedRecord> items) throws Exception {
		for (HarvestedRecord hr : items) {
			DedupRecord dr = hr.getDedupRecord();
			if(dr != null){
				dr.setUpdated(new Date());
				dedupDao.persist(dr);
			}
			recordDao.persist(hr);
		}
	}

}