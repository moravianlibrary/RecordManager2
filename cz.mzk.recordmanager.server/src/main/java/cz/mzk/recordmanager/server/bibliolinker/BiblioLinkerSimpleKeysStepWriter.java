package cz.mzk.recordmanager.server.bibliolinker;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.DedupRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import org.hibernate.SessionFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BiblioLinkerSimpleKeysStepWriter implements
		ItemWriter<List<HarvestedRecord>> {

	@Autowired
	private HarvestedRecordDAO harvestedRecordDAO;

	@Autowired
	private DedupRecordDAO dedupRecordDAO;

	@Autowired
	protected SessionFactory sessionFactory;

	private boolean updateTimestamp;

	public BiblioLinkerSimpleKeysStepWriter(Integer updateTimestamp) {
		this.updateTimestamp = updateTimestamp == null || updateTimestamp.equals(1);
	}

	@Override
	public void write(List<? extends List<HarvestedRecord>> arg0)
			throws Exception {
		Set<DedupRecord> dedupRecords = new HashSet<>();
		for (List<HarvestedRecord> hrList : arg0) {
			for (HarvestedRecord hr : hrList) {
				hr.setBlDisadvantaged(false);
				harvestedRecordDAO.saveOrUpdate(hr);
				if (updateTimestamp) dedupRecords.add(hr.getDedupRecord());
			}
		}
		for (DedupRecord dr : dedupRecords) {
			if (dr == null) continue;
			dr.setUpdated(new Date());
			dedupRecordDAO.saveOrUpdate(dr);
		}
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
	}

}
