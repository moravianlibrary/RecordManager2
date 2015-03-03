package cz.mzk.recordmanager.server.dedup;

import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;

public class HarvestedRecordWriter implements ItemWriter<HarvestedRecord> {

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	@Override
	public void write(List<? extends HarvestedRecord> items) throws Exception {
		for (HarvestedRecord item : items) {
			harvestedRecordDao.persist(item);
		}
	}

}
