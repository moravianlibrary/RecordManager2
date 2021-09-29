package cz.mzk.recordmanager.server.miscellaneous;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.marc.intercepting.MarcInterceptorFactory;
import cz.mzk.recordmanager.server.marc.intercepting.MarcRecordInterceptor;
import cz.mzk.recordmanager.server.model.DownloadImportConfiguration;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.DownloadImportConfigurationDAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.ImportConfigurationMappingFieldDAO;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;
import cz.mzk.recordmanager.server.oai.harvest.SourceMapping;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.ProgressLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class MappedSourceFilterProcessor implements ItemProcessor<List<String>, List<HarvestedRecord>>, StepExecutionListener {

	private static final Logger logger = LoggerFactory.getLogger(MappedSourceFilterProcessor.class);

	private final ProgressLogger progress = new ProgressLogger(logger, 10000);

	@Autowired
	protected HarvestedRecordDAO recordDao;

	@Autowired
	protected OAIHarvestConfigurationDAO configDao;

	@Autowired
	protected DownloadImportConfigurationDAO downloadImportConfDao;

	@Autowired
	private HibernateSessionSynchronizer sync;

	@Autowired
	protected MarcInterceptorFactory marcInterceptorFactory;

	@Autowired
	protected MarcXmlParser marcXmlParser;

	@Autowired
	protected ImportConfigurationMappingFieldDAO importConfigurationMappingFieldDAO;

	private ImportConfiguration mainConfiguration;

	protected final Map<Long, SourceMapping> mapping = new HashMap<>();

	@Override
	public List<HarvestedRecord> process(List<String> items) throws Exception {
		List<HarvestedRecord> results = new ArrayList<>();
		HarvestedRecord mainHr;
		for (String mainRecordId : items) {
			progress.incrementAndLogProgress();
			mainHr = recordDao.get(new HarvestedRecordUniqueId(mainConfiguration, mainRecordId));
			if (mainHr == null) continue;
			MarcRecord marcRecord = marcXmlParser.parseRecord(mainHr.getRawRecord());
			for (Map.Entry<Long, SourceMapping> entry : mapping.entrySet()) {
				if (marcRecord.getFields(entry.getValue().getTag(), entry.getValue().getSubfield()).contains(entry.getValue().getValue())) {
					ImportConfiguration localConfig = entry.getValue().getImportConfiguration();
					HarvestedRecordUniqueId localUniqueId = new HarvestedRecordUniqueId(localConfig, mainRecordId);
					HarvestedRecord localHr = recordDao.get(localUniqueId);
					byte[] recordContent = mainHr.getRawRecord();
					if (recordContent != null && localConfig.isInterceptionEnabled()) {
						MarcRecordInterceptor interceptor = marcInterceptorFactory.getInterceptor(localConfig, localHr.getUniqueId().getRecordId(), recordContent);
						if (interceptor != null) {
							//in case of invalid MARC is error processed later
							recordContent = interceptor.intercept();
						}
					}
					if (localHr == null) {
						localHr = new HarvestedRecord(localUniqueId);
						localHr.setHarvestedFrom(localConfig);
						localHr.setUpdated(mainHr.getUpdated());
						localHr.setOaiTimestamp(mainHr.getOaiTimestamp());
						localHr.setHarvested(mainHr.getHarvested());
						localHr.setFormat(mainHr.getFormat());
					} else if ((localHr.getDeleted() != null && mainHr.getDeleted() != null
							&& (mainHr.getRawRecord() == null || mainHr.getRawRecord().length == 0))
							|| (Arrays.equals(recordContent, localHr.getRawRecord())
							&& ((mainHr.getDeleted() != null) == (localHr.getDeleted() != null)))) {
						localHr.setShouldBeProcessed(false);
						localHr.setLastHarvest(new Date());
						results.add(localHr);
						continue;
					}
					localHr.setShouldBeProcessed(true);
					localHr.setUpdated(new Date());
					localHr.setLastHarvest(new Date());
					if (mainHr.getDeleted() != null) {
						localHr.setDeleted(new Date());
						localHr.setRawRecord(new byte[0]);
					} else {
						localHr.setDeleted(null);
						localHr.setRawRecord(recordContent);
					}
					results.add(localHr);
				}
			}
		}
		return results;
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		try (HibernateSessionSynchronizer.SessionBinder session = sync.register()) {
			Long confId = stepExecution.getJobParameters().getLong("configurationId");
			mainConfiguration = getImportConfiguration(confId);
			try {
				importConfigurationMappingFieldDAO.findByParentImportConf(confId).forEach(config -> {
					SourceMapping childConf = config.getSourceMapping();
					if (childConf != null) {
						mapping.put(childConf.getImportConfiguration().getId(), childConf);
					}
				});
			} catch (Exception e) {

			}
		}
	}

	protected ImportConfiguration getImportConfiguration(Long confId) {
		try {
			OAIHarvestConfiguration hc = configDao.get(confId);
			if (hc != null) {
				return hc;
			} else {
				DownloadImportConfiguration dic = downloadImportConfDao.get(confId);
				if (dic != null) {
					return dic;
				}
			}
		} catch (Exception ex) {

		}
		return null;
	}

}
