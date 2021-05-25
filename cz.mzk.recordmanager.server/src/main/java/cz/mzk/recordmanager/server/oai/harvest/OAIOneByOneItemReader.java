package cz.mzk.recordmanager.server.oai.harvest;

import java.text.ParseException;
import java.util.*;

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

import cz.mzk.recordmanager.server.model.OAIGranularity;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;
import cz.mzk.recordmanager.server.oai.model.OAIGetRecord;
import cz.mzk.recordmanager.server.oai.model.OAIHeader;
import cz.mzk.recordmanager.server.oai.model.OAIIdentify;
import cz.mzk.recordmanager.server.oai.model.OAIListIdentifiers;
import cz.mzk.recordmanager.server.oai.model.OAIRecord;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;

@Component
@StepScope
public class OAIOneByOneItemReader implements ItemReader<List<OAIRecord>>,
		StepExecutionListener, ItemStream {

	@Autowired
	private OAIHarvestConfigurationDAO configDao;

	@Autowired
	private HibernateSessionSynchronizer hibernateSync;

	@Autowired
	private OAIHarvesterFactory harvesterFactory;

	private OAIHarvester harvesterIdentifiers;

	private OAIHarvester harvesterGetRecord;

	// configuration
	private Long confId;

	private Date fromDate;

	private Date untilDate;

	private final boolean reharvest;

	// state
	private String resumptionToken;

	private Queue<OAIHeader> pendingHeadersQueue = new LinkedList<>();

	private boolean finished = false;

	private static final int COMMIT_INTERVAL = 50;

	public OAIOneByOneItemReader(Long confId, Date fromDate, Date untilDate, String resumptionToken, boolean reharvest) {
		super();
		this.confId = confId;
		this.fromDate = fromDate;
		this.untilDate = untilDate;
		this.resumptionToken = resumptionToken;
		this.reharvest = reharvest;
	}

	@Override
	public void beforeStep(final StepExecution stepExecution) {
		try (SessionBinder sess = hibernateSync.register()) {
			OAIHarvestConfiguration conf = configDao.get(confId);
			OAIHarvesterParams paramsIdentifiers = new OAIHarvesterParams();
			if (reharvest && conf.getUrlFullHarvest() != null) paramsIdentifiers.setUrl(conf.getUrlFullHarvest());
			else paramsIdentifiers.setUrl(conf.getUrl());
			paramsIdentifiers.setMetadataPrefix(conf.getMetadataPrefix());
			if (reharvest && conf.getSetFullHarvest() != null) paramsIdentifiers.setSet(conf.getSetFullHarvest());
			else paramsIdentifiers.setSet(conf.getSet());
			paramsIdentifiers.setGranularity(conf.getGranularity());
			try {
				paramsIdentifiers.setFrom(fromDate);
			} catch (ParseException e) {
				throw new RuntimeException("Cannot parse 'from' parameter", e);
			}
			paramsIdentifiers.setUntil(untilDate);
			harvesterIdentifiers = harvesterFactory.create(paramsIdentifiers);
			processIdentify(conf);
			conf = configDao.get(confId);
			paramsIdentifiers.setGranularity(conf.getGranularity());
			harvesterIdentifiers = harvesterFactory.create(paramsIdentifiers);
			// GetRecord without from and until
			OAIHarvesterParams paramsGetRecord = new OAIHarvesterParams();
			if (reharvest && conf.getUrlFullHarvest() != null) paramsGetRecord.setUrl(conf.getUrlFullHarvest());
			else paramsGetRecord.setUrl(conf.getUrl());
			paramsGetRecord.setMetadataPrefix(conf.getMetadataPrefix());
			harvesterGetRecord = harvesterFactory.create(paramsGetRecord);
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

	/**
	 * reads content of pendingHeadersQueue queue and processes records.
	 */
	@Override
	public List<OAIRecord> read() {
		prepareHeadersQueue();
		List<OAIRecord> result = new ArrayList<>();
		Set<String> idsInToken = new HashSet<>();
		for (int i = 0; i < Math.min(COMMIT_INTERVAL, pendingHeadersQueue.size()); i++) {
			OAIHeader header = pendingHeadersQueue.poll();
			if (!idsInToken.add(header.getIdentifier())) continue; // harvest only once
			OAIGetRecord oaiGetRecord = harvesterGetRecord.getRecord(header.getIdentifier());
			if (oaiGetRecord != null && oaiGetRecord.getRecord() != null) {
				result.add(oaiGetRecord.getRecord());
			}
		}

		return result.isEmpty() ? null : result;
	}

	/**
	 * prepares pendingHeadersQueue. Calls ListIdentfiers verb via OAI if there aren't
	 * any pending headers left.
	 */
	protected void prepareHeadersQueue() {
		if (pendingHeadersQueue.isEmpty()) {
			if (finished) {
				return;
			}
			OAIListIdentifiers listIdentifiers = harvesterIdentifiers.listIdentifiers(resumptionToken);
			if (listIdentifiers == null) return; // 'no records match'
			pendingHeadersQueue.addAll(listIdentifiers.getHeaders());
			resumptionToken = listIdentifiers.getNextResumptionToken();
			if (resumptionToken == null || resumptionToken.isEmpty()) {
				finished = true;
			}
		}
	}

	/**
	 * process Identify request and update stored
	 * {@link OAIHarvestConfiguration}
	 */
	protected void processIdentify(OAIHarvestConfiguration conf) {
		OAIIdentify identify = harvesterIdentifiers.identify();
		conf.setGranularity(OAIGranularity.stringToOAIGranularity(identify
				.getGranularity()));
		configDao.persist(conf);
	}

	@Override
	public void open(ExecutionContext ctx)
			throws ItemStreamException {
		if (ctx.containsKey("resumptionToken")) {
			resumptionToken = ctx.getString("resumptionToken");
		}
	}

	@Override
	public void update(ExecutionContext ctx)
			throws ItemStreamException {
		ctx.putString("resumptionToken", resumptionToken);

	}

	@Override
	public void close() throws ItemStreamException {
	}

}
