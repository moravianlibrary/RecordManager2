package cz.mzk.recordmanager.server.oai.harvest;

import java.util.Date;
import java.util.List;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;
import cz.mzk.recordmanager.server.oai.model.OAIIdentify;
import cz.mzk.recordmanager.server.oai.model.OAIListRecords;
import cz.mzk.recordmanager.server.oai.model.OAIRecord;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;

@Component
@StepScope
public class OAIItemReader implements ItemReader<List<OAIRecord>>, ItemStream,
		StepExecutionListener {

	@Autowired
	private OAIHarvestConfigurationDAO configDao;

	@Autowired
	private OAIHarvesterFactory harvesterFactory;

	@Autowired
	private TransactionTemplate template;

	@Autowired
	private HibernateSessionSynchronizer sync;

	private OAIHarvester harvester;

	// state
	private String resumptionToken;

	private boolean finished = false;

	private Long confId;
	
	@Override
	public List<OAIRecord> read() {
		if (finished) {
			return null;
		}
		
		OAIListRecords listRecords = harvester.listRecords(resumptionToken);
		resumptionToken = listRecords.getNextResumptionToken();
		if (resumptionToken == null) {
			finished = true;
		}
		if (listRecords.getRecords().isEmpty()) {
			return null;
		} else {
			return listRecords.getRecords();
		}
	}

	@Override
	public void close() throws ItemStreamException {
	}

	@Override
	public void open(ExecutionContext ctx) throws ItemStreamException {
		if (ctx.containsKey("resumptionToken")) {
			resumptionToken = ctx.getString("resumptionToken");
		}
	}

	@Override
	public void update(ExecutionContext ctx) throws ItemStreamException {
		ctx.putString("resumptionToken", resumptionToken);
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

	@Override
	public void beforeStep(final StepExecution stepExecution) {
		try (SessionBinder session = sync.register()) {
			confId = stepExecution.getJobParameters().getLong(
					Constants.JOB_PARAM_CONF_ID);
			OAIHarvestConfiguration conf = configDao.get(confId);
			OAIHarvesterParams params = new OAIHarvesterParams();
			params.setUrl(conf.getUrl());
			params.setMetadataPrefix(conf.getMetadataPrefix());
			params.setGranularity(conf.getGranularity());
			Date fromDate = stepExecution.getJobParameters().getDate(Constants.JOB_PARAM_FROM_DATE);
			if (fromDate != null) {
				params.setFrom(fromDate);
			}
			Date untilDate = stepExecution.getJobParameters().getDate(Constants.JOB_PARAM_UNTIL_DATE);
			if (untilDate != null) {
				params.setUntil(untilDate);
			}
			harvester = harvesterFactory.create(params);
			processIdentify(conf);
		}
	}
	
	/**
	 * process Identify request and update stored {@link OAIHarvestConfiguration}
	 */
	private void processIdentify(OAIHarvestConfiguration conf) {
		OAIIdentify identify = harvester.identify();
		conf.setGranularity(identify.getGranularity());
		configDao.persist(conf);
	}

}
