package cz.mzk.recordmanager.server.imports.inspirations;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordInspiration;
import cz.mzk.recordmanager.server.oai.dao.DedupRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordInspirationDAO;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public class InspirationDeleteWriter implements ItemWriter<Long> {

	@Autowired
	private DedupRecordDAO drDao;

	@Autowired
	private HarvestedRecordInspirationDAO insDao;

	@Override
	public void write(List<? extends Long> items) throws Exception {
		for (Long id : items) {
			HarvestedRecordInspiration ins = insDao.get(id);
			if (ins == null) continue;
			DedupRecord dr = ins.getHarvestedRecord().getDedupRecord();
			if (dr != null) {
				dr.setUpdated(new Date());
				drDao.saveOrUpdate(dr);
			}
			insDao.delete(ins);
		}
	}
}
