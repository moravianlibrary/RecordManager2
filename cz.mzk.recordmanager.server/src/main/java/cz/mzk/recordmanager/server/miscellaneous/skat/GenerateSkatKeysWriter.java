package cz.mzk.recordmanager.server.miscellaneous.skat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.model.Sigla;
import cz.mzk.recordmanager.server.model.SkatKey;
import cz.mzk.recordmanager.server.oai.dao.SkatKeyDAO;

public class GenerateSkatKeysWriter implements ItemWriter<Set<SkatKey>>, StepExecutionListener {

	@Autowired
	private SkatKeyDAO skatKeyDao;

	@Autowired
	private SessionFactory sessionFactory;

	private static HashMap<String, List<Long>> siglas;

	@Override
	public void write(List<? extends Set<SkatKey>> items) throws Exception {
		Long lastSkatId = -1L;
		for (Set<SkatKey> list: items) {
			for (SkatKey key: list) {
				skatKeyDao.persist(key);

				if (lastSkatId == key.getSkatKeyId().getSkatHarvestedRecordId()) continue;
				List<Long> import_confs = siglas.get(key.getSkatKeyId().getSigla());
				if (import_confs == null) continue;
				for (Long import_conf: import_confs) {
					String query = "UPDATE harvested_record SET next_dedup_flag=true WHERE import_conf_id = ? AND raw_001_id = ? ";
					Session session = sessionFactory.getCurrentSession();
					int status = session.createSQLQuery(query)
						.setLong(0, import_conf)
						.setString(1, key.getSkatKeyId().getRecordId())
						.executeUpdate();
					if (status > 0) {
						lastSkatId = key.getSkatKeyId().getSkatHarvestedRecordId();
						break;
					}
				}
			}
		}
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		siglas = new HashMap<>();
		Sigla sigla;
		List<Long> import_conf;
		for (Object obj : sessionFactory.openSession().createCriteria(Sigla.class).list()) {
			sigla = (Sigla) obj;
			if ((import_conf = siglas.remove(sigla.getUniqueId().getSigla())) != null) {
				import_conf.add(sigla.getUniqueId().getImportConfId());
				siglas.put(sigla.getUniqueId().getSigla(), import_conf);
			} else {
				siglas.put(sigla.getUniqueId().getSigla(), new ArrayList<>(Arrays.asList(sigla.getUniqueId().getImportConfId())));
			}
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}


	
}
