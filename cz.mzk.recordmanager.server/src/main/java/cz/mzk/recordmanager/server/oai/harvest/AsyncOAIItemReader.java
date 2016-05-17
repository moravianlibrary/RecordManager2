package cz.mzk.recordmanager.server.oai.harvest;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import cz.mzk.recordmanager.server.model.OAIGranularity;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;
import cz.mzk.recordmanager.server.oai.model.OAIIdentify;
import cz.mzk.recordmanager.server.oai.model.OAIListRecords;
import cz.mzk.recordmanager.server.oai.model.OAIRecord;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;

public class AsyncOAIItemReader implements ItemReader<List<OAIRecord>>, ItemStream,
		StepExecutionListener {

	@Autowired
	private OAIHarvestConfigurationDAO configDao;

	@Autowired
	private OAIHarvesterFactory harvesterFactory;

	@Autowired
	private TransactionTemplate template;

	@Autowired
	private HibernateSessionSynchronizer hibernateSync;

	private OAIHarvester harvester;

	// configuration
	private Long confId;

	private Date fromDate;

	private Date untilDate;

	private String resumptionToken;

	private boolean done = false;

	// state
	private String lastReturnedResumptionToken;

	private static final Entry END_SENTINEL = new Entry(null, Collections.emptyList());

	private static class Entry {

		private final String resumptionToken;

		private final List<OAIRecord> records;

		public Entry(String resumptionToken, List<OAIRecord> records) {
			super();
			this.resumptionToken = resumptionToken;
			this.records = records;
		}

	}

	private Thread fillingThread;

	private ArrayBlockingQueue<Entry> queue = new ArrayBlockingQueue<Entry>(5);

	public AsyncOAIItemReader(Long confId, Date fromDate, Date untilDate, String resumptionToken) {
		super();
		this.confId = confId;
		this.fromDate = fromDate;
		this.untilDate = untilDate;
		this.resumptionToken = resumptionToken;
	}

	@Override
	public List<OAIRecord> read() {
		if (done) {
			return null;
		}
		Entry entry;
		try {
			entry = queue.take();
		} catch (InterruptedException e) {
			done = true;
			return null;
		}
		List<OAIRecord> result = (entry == null || entry == END_SENTINEL || entry.records == null) ? null : entry.records;
		lastReturnedResumptionToken = (entry != null) ? entry.resumptionToken : null;
		if (result == null) {
			done = true;
		}
		return result;
	}

	@Override
	public void close() throws ItemStreamException {
		if (fillingThread != null) {
			fillingThread.interrupt();
		}
	}

	@Override
	public void open(ExecutionContext ctx) throws ItemStreamException {
		if (ctx.containsKey("resumptionToken")) {
			resumptionToken = ctx.getString("resumptionToken");
			lastReturnedResumptionToken = resumptionToken;
		}
		fillingThread = new Thread((Runnable) this::queueWriter);
		fillingThread.start();
	}

	@Override
	public void update(ExecutionContext ctx) throws ItemStreamException {
		ctx.putString("resumptionToken", lastReturnedResumptionToken);
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
			params.setFrom(fromDate);
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

	private void queueWriter() {
		try {
			do {
				OAIListRecords listRecords = harvester
						.listRecords(resumptionToken);
				if (listRecords != null) {
					resumptionToken = listRecords.getNextResumptionToken();
				}
				List<OAIRecord> records = null;
				if (listRecords != null && listRecords.getRecords() != null
						&& !listRecords.getRecords().isEmpty()) {
					records = listRecords.getRecords();
				}
				try {
					queue.put(new Entry(resumptionToken, records));
				} catch (InterruptedException ie) {
					break;
				}
			} while (resumptionToken != null);
		} finally {
			try {
				queue.put(END_SENTINEL);
			} catch (InterruptedException ie) {
				// done
			}
		}
	}

}
