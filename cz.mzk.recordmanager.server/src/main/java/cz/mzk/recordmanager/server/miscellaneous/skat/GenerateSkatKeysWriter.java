package cz.mzk.recordmanager.server.miscellaneous.skat;

import java.util.List;
import java.util.Set;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.model.SkatKey;
import cz.mzk.recordmanager.server.oai.dao.SkatKeyDAO;

public class GenerateSkatKeysWriter extends GenerateSkatKeysProcessor implements
		ItemWriter<Set<SkatKey>> {

	@Autowired
	private SkatKeyDAO skatKeyDao;
	
	@Override
	public void write(List<? extends Set<SkatKey>> items) throws Exception {
		
		for (Set<SkatKey> list: items) {
			for (SkatKey key: list) {
				skatKeyDao.persist(key);
			}
		}
	}


	
}
