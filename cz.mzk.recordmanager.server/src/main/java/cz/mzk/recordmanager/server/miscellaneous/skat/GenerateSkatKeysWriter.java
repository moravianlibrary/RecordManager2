package cz.mzk.recordmanager.server.miscellaneous.skat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.model.SkatKey;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.SkatKeyDAO;

public class GenerateSkatKeysWriter extends GenerateSkatKeysProcessor implements
		ItemWriter<Set<SkatKey>> {

	@Autowired
	private SkatKeyDAO skatKeyDao;

	@Autowired
	private HarvestedRecordDAO hrDao;

	@Autowired
	private SessionFactory sessionFactory;
	
	@Override
	public void write(List<? extends Set<SkatKey>> items) throws Exception {
		
		for (Set<SkatKey> list: items) {
			Set<Long> hrIds = new HashSet<Long>();
			for (SkatKey key: list) {
				skatKeyDao.persist(key);
				hrIds.add(key.getSkatKeyId().getSkatHarvestedRecordId());
			}
			for (Long hrId : hrIds) {
				String query = "UPDATE harvested_record SET next_dedup_flag=true WHERE id = ?";
				Session session = sessionFactory.getCurrentSession();
				session.createSQLQuery(query).setLong(0, hrId).executeUpdate();
			}
		}
	}


	
}
