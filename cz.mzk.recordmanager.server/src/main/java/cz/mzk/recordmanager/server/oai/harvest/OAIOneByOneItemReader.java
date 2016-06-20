package cz.mzk.recordmanager.server.oai.harvest;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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
	
	private OAIHarvester harvester;
	
	// configuration
	private Long confId;

	private Date fromDate;

	private Date untilDate;

	// state
	private String resumptionToken;
	
	private Queue<OAIHeader> pendingHeadersQueue = new LinkedList<OAIHeader>();

	private boolean finished = false;
	
    private static final int COMMIT_INTERVAL = 50;
	
	public OAIOneByOneItemReader(Long confId, Date fromDate, Date untilDate, String resumptionToken) {
		super();
		this.confId = confId;
		this.fromDate = fromDate;
		this.untilDate = untilDate;
		this.resumptionToken = resumptionToken;
	}
	
	@Override
	public void beforeStep(final StepExecution stepExecution) {
		try (SessionBinder sess = hibernateSync.register()) {
			OAIHarvestConfiguration conf = configDao.get(confId);
			OAIHarvesterParams params = new OAIHarvesterParams();
			params.setUrl(conf.getUrl());
			params.setMetadataPrefix(conf.getMetadataPrefix());
			params.setSet(conf.getSet());
			params.setGranularity(conf.getGranularity());
			params.setFrom(fromDate);
			params.setUntil(untilDate);
			harvester = harvesterFactory.create(params);
			processIdentify(conf);
			conf = configDao.get(confId);
			params.setGranularity(conf.getGranularity());
			harvester = harvesterFactory.create(params);
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * reads content of pendingHeadersQueue queue and processes records.
	 */
	@Override
	public List<OAIRecord> read() {
		prepareHeadersQueue();
		List<OAIRecord> result = new ArrayList<OAIRecord>();
		for (int i = 0; i < Math.min(COMMIT_INTERVAL,pendingHeadersQueue.size()); i++) {
			OAIHeader header = pendingHeadersQueue.poll();
			OAIGetRecord oaiGetRecord = harvester.getRecord(header.getIdentifier());
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
			OAIListIdentifiers listIdentifiers = harvester.listIdentifiers(resumptionToken);
			for (OAIHeader currentHeader: listIdentifiers.getHeaders()) {
				pendingHeadersQueue.add(currentHeader);
			}
			resumptionToken = listIdentifiers.getNextResumptionToken();
			if (resumptionToken == null) {
				finished = true;
			}
		}		
	}  
	
	/**
	 * process Identify request and update stored
	 * {@link OAIHarvestConfiguration}
	 */
	protected void processIdentify(OAIHarvestConfiguration conf) {
		OAIIdentify identify = harvester.identify();
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
