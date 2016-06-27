package cz.mzk.recordmanager.server.oai.harvest;

import java.util.Date;
import java.util.List;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.dc.InvalidDcException;
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
			for (HarvestedRecord record : list) {
				if (record == null) {
					continue;
				}
				if (!record.getShouldBeProcessed()) {
					recordDao.updateTimestampOnly(record);
					continue;
				}
				if (record.getDeleted() == null) {
					try {
						if (record.getId() == null) {
							recordDao.persist(record);
						}
						if (record.getHarvestedFrom().isFilteringEnabled() && !record.getShouldBeProcessed()) {
							logger.debug("Filtered record: " + record.getUniqueId());
							record.setDeleted(new Date());
						} else {
							dedupKeysParser.parse(record);
						}
					} catch (DedupKeyParserException dkpe) {
						logger.error(
								"Dedup keys could not be generated for {}, exception thrown.",
								record, dkpe);
					} catch (InvalidMarcException ime) {
						logger.warn("Skipping record due to invalid MARC {}", record.getUniqueId());
					} catch (InvalidDcException dce) {
						logger.warn("Skipping record due to invalid DublinCore {}", record.getUniqueId());
					}
					
				}
				recordDao.persist(record);
				
			}
		}
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
	}

}
