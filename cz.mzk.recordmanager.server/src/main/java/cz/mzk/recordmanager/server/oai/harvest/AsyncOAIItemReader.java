package cz.mzk.recordmanager.server.oai.harvest;

import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.beans.factory.annotation.Autowired;

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

	private static class Entry {

		private final String resumptionToken;

		private final List<OAIRecord> records;

		public Entry(String resumptionToken, List<OAIRecord> records) {
			super();
			this.resumptionToken = resumptionToken;
			this.records = records;
		}

	}

	private static Logger logger = LoggerFactory.getLogger(OAIHarvesterImpl.class);

	private static final Entry HARVEST_FINISHED_SENTINEL = new Entry(null, Collections.emptyList());

	private static final Entry HARVEST_FAILED_SENTINEL = new Entry(null, Collections.emptyList());

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

	private String resumptionToken;

	private boolean done = false;

	// state
	private String lastReturnedResumptionToken;

	private volatile Thread harvestingThread;

	private ArrayBlockingQueue<Entry> queue = new ArrayBlockingQueue<Entry>(5);

	public AsyncOAIItemReader(Long confId, Date fromDate, Date untilDate, String resumptionToken) {
		super();
		this.confId = confId;
		this.fromDate = fromDate;
		this.untilDate = untilDate;
		this.resumptionToken = resumptionToken;
	}

	@Override
	public List<OAIRecord> read() throws InterruptedException {
		if (done) {
			return null;
		}
		Entry entry = queue.take();
		if (entry == HARVEST_FINISHED_SENTINEL) {
			done = true;
			return null;
		} else if (entry == HARVEST_FAILED_SENTINEL) {
			done = true;
			throw new RuntimeException("OAI harvest failed, see logs for details");
		}
		lastReturnedResumptionToken = entry.resumptionToken;
		return entry.records;
	}

	@Override
	public void close() throws ItemStreamException {
		if (harvestingThread != null) {
			harvestingThread.interrupt();
		}
	}

	@Override
	public void open(ExecutionContext ctx) throws ItemStreamException {
		if (ctx.containsKey("resumptionToken")) {
			resumptionToken = ctx.getString("resumptionToken");
			lastReturnedResumptionToken = resumptionToken;
		}
		harvestingThread = new Thread(this::queueWriter);
		harvestingThread.start();
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

	private void queueWriter() {
		boolean failed = true;
		try {
			while (true) {
				OAIListRecords listRecords = harvester.listRecords(resumptionToken);
				resumptionToken = (listRecords != null)? listRecords.getNextResumptionToken() : null;
				if (resumptionToken != null && resumptionToken.isEmpty()) resumptionToken = null;
				if (listRecords == null || listRecords.getRecords() == null
						|| listRecords.getRecords().isEmpty()) {
					failed = false;
					break;
				}
				try {
					queue.put(new Entry(resumptionToken, listRecords.getRecords()));
				} catch (InterruptedException ie) {
					// to make sure there is a room for sentinel value
					if (queue.remainingCapacity() == 0) {
						queue.poll();
					}
					break;
				}
				if (resumptionToken == null) {
					failed = false;
					break;
				}
			}
		} catch (RuntimeException re) {
			logger.error("Exception thrown during OAI harvest", re);
		} finally {
			try {
				queue.put((failed) ? HARVEST_FAILED_SENTINEL : HARVEST_FINISHED_SENTINEL);
			} catch (InterruptedException ie) {
				queue.poll(); // to make sure there is a room for sentinel value
				try {
					queue.put((failed) ? HARVEST_FAILED_SENTINEL : HARVEST_FINISHED_SENTINEL);
				} catch (InterruptedException ie2) {
					// done
				}
			}
		}
		this.harvestingThread = null;
	}

}
