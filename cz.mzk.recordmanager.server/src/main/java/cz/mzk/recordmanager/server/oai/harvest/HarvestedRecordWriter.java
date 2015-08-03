package cz.mzk.recordmanager.server.oai.harvest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.dedup.DedupKeyParserException;
import cz.mzk.recordmanager.server.dedup.DelegatingDedupKeysParser;
import cz.mzk.recordmanager.server.marc.InvalidMarcException;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;

@Component
public class HarvestedRecordWriter implements ItemWriter<List<HarvestedRecord>> {

	private static Logger logger = LoggerFactory.getLogger(HarvestedRecordWriter.class);
	
	@Autowired
	protected HarvestedRecordDAO recordDao;

	@Autowired
	protected DelegatingDedupKeysParser dedupKeysParser;
	
	@Override
	public void write(List<? extends List<HarvestedRecord>> arg0) throws Exception {
		for (List<HarvestedRecord> list: arg0) {
			for (HarvestedRecord hr: list) {
				if (hr.getDeleted() == null) {
					try {
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
		
	}

}
