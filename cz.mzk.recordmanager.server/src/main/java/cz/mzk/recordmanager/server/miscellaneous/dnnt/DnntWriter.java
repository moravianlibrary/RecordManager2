package cz.mzk.recordmanager.server.miscellaneous.dnnt;

import cz.mzk.recordmanager.server.marc.InvalidMarcException;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.ProgressLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@StepScope
public class DnntWriter implements ItemWriter<Long> {

	private static Logger logger = LoggerFactory.getLogger(DnntWriter.class);

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	@Autowired
	private MetadataRecordFactory mrfactory;

	private ProgressLogger progressLogger = new ProgressLogger(logger, 10000);

	@Override
	public void write(List<? extends Long> ids) {
		for (Long id : ids) {
			HarvestedRecord rec = harvestedRecordDao.get(id);

			progressLogger.incrementAndLogProgress(rec);
			if (!rec.getHarvestedFrom().isGenerateDedupKeys()
					|| rec.getRawRecord() == null || rec.getRawRecord().length == 0) {
				continue;
			}
			try {
				Long oldLoans = rec.getLoans();
				Long newLoans = mrfactory.getMetadataRecord(rec).getLoanRelevance();
				if (!Objects.equals(oldLoans, newLoans) && newLoans != 0) {
					rec.setLoans(newLoans);
					harvestedRecordDao.saveOrUpdate(rec);
				}
			} catch (InvalidMarcException ime) {
				logger.warn("Invalid Marc in record: " + rec.getId());
			} catch (Exception e) {
				logger.warn("Skipping record due to error: " + e);
			}

		}
	}
}
