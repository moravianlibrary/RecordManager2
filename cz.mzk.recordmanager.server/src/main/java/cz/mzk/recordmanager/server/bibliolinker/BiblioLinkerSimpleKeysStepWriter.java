package cz.mzk.recordmanager.server.bibliolinker;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import org.hibernate.SessionFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class BiblioLinkerSimpleKeysStepWriter implements
		ItemWriter<List<HarvestedRecord>> {

	@Autowired
	private HarvestedRecordDAO harvestedRecordDAO;

	@Autowired
	protected SessionFactory sessionFactory;

	@Override
	public void write(List<? extends List<HarvestedRecord>> arg0)
			throws Exception {
		for (List<HarvestedRecord> hrList : arg0) {
			for (HarvestedRecord hr : hrList) {
				hr.setBlDisadvantaged(false);
				harvestedRecordDAO.saveOrUpdate(hr);
			}
		}
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
	}

}
