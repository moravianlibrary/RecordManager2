package cz.mzk.recordmanager.server.kramerius.fulltext;

import java.util.Date;
import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;

public class KrameriusFulltextWriter implements ItemWriter<HarvestedRecord> {

	@Autowired
	private HarvestedRecordDAO recordDao;

	@Autowired
	private HibernateSessionSynchronizer sync;

	@Override
	public void write(List<? extends HarvestedRecord> items) throws Exception {
		for (HarvestedRecord hr : items) {
			hr.setUpdated(new Date());
			recordDao.persist(hr);
		}
	}

}