package cz.mzk.recordmanager.server.miscellaneous.skat;

import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.model.SkatKey;
import cz.mzk.recordmanager.server.oai.dao.SkatKeyDAO;

public class GenerateSkatKeysWriter extends GenerateSkatKeysProcessor implements
		ItemWriter<List<SkatKey>> {

	@Autowired
	private SkatKeyDAO skatKeyDao;
	
	@Override
	public void write(List<? extends List<SkatKey>> items) throws Exception {
		
		for (List<SkatKey> list: items) {
			for (SkatKey key: list) {
				skatKeyDao.persist(key);
			}
		}
	}

	
}
