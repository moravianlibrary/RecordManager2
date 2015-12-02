package cz.mzk.recordmanager.server.oai.harvest;

import java.util.List;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.dedup.DedupKeyParserException;
import cz.mzk.recordmanager.server.dedup.DelegatingDedupKeysParser;
import cz.mzk.recordmanager.server.marc.InvalidMarcException;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;

public class HarvestedRecordWriter implements ItemWriter<List<HarvestedRecord>> {

	private static Logger logger = LoggerFactory.getLogger(HarvestedRecordWriter.class);

	@Autowired
	protected HarvestedRecordDAO recordDao;

	@Autowired
	protected DelegatingDedupKeysParser dedupKeysParser;

	@Autowired
	protected SessionFactory sessionFactory;

	@Override
	public void write(List<? extends List<HarvestedRecord>> records) throws Exception {
		for (List<HarvestedRecord> list: records) {
			for (HarvestedRecord hr: list) {
				if(hr == null) continue;
				if (hr.getDeleted() == null) {
					try {
						if (hr.getId() == null) {
							recordDao.persist(hr);
						}
						dedupKeysParser.parse(hr);
						if (hr.getHarvestedFrom().isFilteringEnabled() && !hr.getShouldBeProcessed()) {
							logger.debug("Filtered record: " + hr.getUniqueId());
							return;
						}
					} catch (DedupKeyParserException dkpe) {
						logger.error(
								"Dedup keys could not be generated for {}, exception thrown.",
								hr, dkpe);
					} catch (InvalidMarcException ime) {
						logger.warn("Skipping record due to invalid MARC {}", hr.getUniqueId());
					}
				}
				recordDao.persist(hr);
				
			}
		}
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
	}

}
