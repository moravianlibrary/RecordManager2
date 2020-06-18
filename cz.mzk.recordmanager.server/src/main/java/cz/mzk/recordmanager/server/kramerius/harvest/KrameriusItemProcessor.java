package cz.mzk.recordmanager.server.kramerius.harvest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.KrameriusConfiguration;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.KrameriusConfigurationDAO;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;

public class KrameriusItemProcessor implements
		ItemProcessor<List<HarvestedRecord>, List<HarvestedRecord>>,
		StepExecutionListener {

	@Autowired
	protected HarvestedRecordDAO recordDao;

	@Autowired
	protected KrameriusConfigurationDAO configDao;

	@Autowired
	protected KrameriusFormatResolver formatResolver;

	@Autowired
	private HibernateSessionSynchronizer sync;

	private String format;
	private KrameriusConfiguration configuration;

	@Override
	public List<HarvestedRecord> process(List<HarvestedRecord> arg0)
			throws Exception {
		List<HarvestedRecord> result = new ArrayList<>();
		for (HarvestedRecord hrIncomplete : arg0) {
			result.add(completeHarvestedRecord(hrIncomplete));
		}
		return result;
	}

	private HarvestedRecord completeHarvestedRecord(HarvestedRecord hrIncomplete) {
		String recordId = hrIncomplete.getUniqueId().getRecordId();
		HarvestedRecord rec = recordDao.findByIdAndHarvestConfiguration(
				recordId, configuration);
		if (rec == null) {
			rec = hrIncomplete;
			rec.setHarvestedFrom(configuration);
		} else if (Arrays.equals(hrIncomplete.getRawRecord(), rec.getRawRecord())) {
			rec.setDeleted(null);
			rec.setLastHarvest(new Date());
			return rec; // no change in record
		}
		rec.setFormat(format);
		rec.setUpdated(new Date());
		rec.setLastHarvest(new Date());
		rec.setDeleted(null);
		rec.setRawRecord(hrIncomplete.getRawRecord());
		return rec;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		try (SessionBinder session = sync.register()) {
			Long confId = stepExecution.getJobParameters().getLong(
					"configurationId");
			configuration = configDao.get(confId);
			format = formatResolver.resolve(configuration.getMetadataStream());
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

}
