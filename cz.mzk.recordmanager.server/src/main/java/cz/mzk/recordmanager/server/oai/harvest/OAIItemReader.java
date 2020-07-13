package cz.mzk.recordmanager.server.oai.harvest;

import cz.mzk.recordmanager.server.model.OAIGranularity;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;
import cz.mzk.recordmanager.server.oai.model.OAIIdentify;
import cz.mzk.recordmanager.server.oai.model.OAIListRecords;
import cz.mzk.recordmanager.server.oai.model.OAIRecord;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class OAIItemReader implements ItemReader<List<OAIRecord>>, ItemStream,
		StepExecutionListener {

	@Autowired
	private OAIHarvestConfigurationDAO configDao;

	@Autowired
	private OAIHarvesterFactory harvesterFactory;

	@Autowired
	private HibernateSessionSynchronizer hibernateSync;

	private OAIHarvester harvester;

	// configuration
	private Long confId;

	private Date fromDate;

	private Date untilDate;

	// state
	private String resumptionToken;

	private boolean finished = false;

	public OAIItemReader(Long confId, Date fromDate, Date untilDate, String resumptionToken) {
		super();
		this.confId = confId;
		this.fromDate = fromDate;
		this.untilDate = untilDate;
		this.resumptionToken = resumptionToken;
	}

	@Override
	public List<OAIRecord> read() {
		if (finished) {
			return null;
		}

		OAIListRecords listRecords = harvester.listRecords(resumptionToken);
		if (listRecords == null) {
			return null;
		}
		resumptionToken = listRecords.getNextResumptionToken();
		if (resumptionToken == null || resumptionToken.isEmpty()) {
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
		try (SessionBinder sess = hibernateSync.register()) {
			OAIHarvestConfiguration conf = configDao.get(confId);
			OAIHarvesterParams params = new OAIHarvesterParams();
			params.setUrl(conf.getUrl());
			params.setMetadataPrefix(conf.getMetadataPrefix());
			params.setGranularity(conf.getGranularity());
			params.setSet(conf.getSet());
			try {
				params.setFrom(fromDate);
			} catch (ParseException e) {
				throw new RuntimeException("Cannot parse 'from' parameter", e);
			}
			params.setUntil(untilDate);
			harvester = harvesterFactory.create(params);
			processIdentify(conf);
			conf = configDao.get(confId);
			params.setGranularity(conf.getGranularity());
			harvester = harvesterFactory.create(params);
		}
	}

	/**
	 * process Identify request and update stored
	 * {@link OAIHarvestConfiguration}
	 */
	private void processIdentify(OAIHarvestConfiguration conf) {
		OAIIdentify identify = harvester.identify();
		conf.setGranularity(OAIGranularity.stringToOAIGranularity(identify
				.getGranularity()));
		configDao.persist(conf);
	}

}
