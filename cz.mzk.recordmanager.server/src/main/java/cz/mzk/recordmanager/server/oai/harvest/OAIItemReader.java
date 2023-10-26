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
import java.util.ArrayList;
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

	private final boolean reharvest;

	// state
	private String resumptionToken;

	private boolean finished = false;

	private List<OAIRecord> batchRecords = new ArrayList<>();

	private static final int BATCH_SIZE = 100;

	public OAIItemReader(Long confId, Date fromDate, Date untilDate, String resumptionToken, boolean reharvest) {
		super();
		this.confId = confId;
		this.fromDate = fromDate;
		this.untilDate = untilDate;
		this.resumptionToken = resumptionToken;
		this.reharvest = reharvest;
	}

	@Override
	public List<OAIRecord> read() {
		if (finished && batchRecords.isEmpty()) {
			return null;
		}

		if (!batchRecords.isEmpty()) {
			return getRecords();
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
			batchRecords = listRecords.getRecords();
			return getRecords();
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
			if (reharvest && conf.getUrlFullHarvest() != null) params.setUrl(conf.getUrlFullHarvest());
			else params.setUrl(conf.getUrl());
			params.setMetadataPrefix(conf.getMetadataPrefix());
			params.setGranularity(conf.getGranularity());
			if (reharvest && conf.getSetFullHarvest() != null) params.setSet(conf.getSetFullHarvest());
			else params.setSet(conf.getSet());
			try {
				params.setFrom(fromDate);
			} catch (ParseException e) {
				throw new RuntimeException("Cannot parse 'from' parameter", e);
			}
			params.setUntil(untilDate);
			params.setIctx(conf.getIctx());
			params.setOp(conf.getOp());
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

	private List<OAIRecord> getRecords() {
		List<OAIRecord> results = new ArrayList<>();
		int i = 0;
		while (i < BATCH_SIZE && !batchRecords.isEmpty()) {
			results.add(batchRecords.remove(0));
			++i;
		}
		return results;
	}
	
}
