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
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;
import cz.mzk.recordmanager.server.oai.harvest.SourceMapping;
import cz.mzk.recordmanager.server.scripting.MappingResolver;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.ProgressLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private MappingResolver propertyResolver;

	private ImportConfiguration configuration;

	protected final Map<String, SourceMapping> mapping = new HashMap<>();

	private static final Pattern FIELD_VALUE = Pattern.compile("([0-9]{3})\\$(.)(.*)", Pattern.CASE_INSENSITIVE);

	@Override
	public List<HarvestedRecord> process(List<String> items) throws Exception {
		List<HarvestedRecord> results = new ArrayList<>();
		HarvestedRecord hr;
		for (String item : items) {
			progress.incrementAndLogProgress();
			hr = recordDao.get(new HarvestedRecordUniqueId(configuration, item));
			if (hr == null) continue;
			MarcRecord marcRecord = marcXmlParser.parseRecord(hr.getRawRecord());
			for (Map.Entry<String, SourceMapping> entry : mapping.entrySet()) {
				if (marcRecord.getFields(entry.getValue().getTag(), entry.getValue().getSubfield()).contains(entry.getValue().getValue())) {
					ImportConfiguration config = getImportConfiguration(Long.parseLong(entry.getKey()));
					HarvestedRecordUniqueId newId = new HarvestedRecordUniqueId(config, item);
					if (recordDao.get(newId) != null) continue;
					HarvestedRecord newHr = new HarvestedRecord(newId);
					newHr.setHarvestedFrom(config);
					newHr.setUpdated(hr.getUpdated());
					newHr.setOaiTimestamp(hr.getOaiTimestamp());
					newHr.setHarvested(hr.getHarvested());
					newHr.setFormat(hr.getFormat());
					byte[] recordContent = hr.getRawRecord();
					if (recordContent != null && config.isInterceptionEnabled()) {
						MarcRecordInterceptor interceptor = marcInterceptorFactory.getInterceptor(config, newHr.getUniqueId().getRecordId(), recordContent);
						if (interceptor != null) {
							//in case of invalid MARC is error processed later
							recordContent = interceptor.intercept();
						}
					}
					newHr.setRawRecord(recordContent);
					results.add(newHr);
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
			configuration = getImportConfiguration(confId);
			try {
				propertyResolver.resolve(confId + ".map").getMapping().forEach((key, value) -> {
					Matcher matcher = FIELD_VALUE.matcher(value.get(0));
					if (matcher.matches()) {
						mapping.put(key, new SourceMapping(matcher.group(1), matcher.group(2).charAt(0), matcher.group(3)));
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
