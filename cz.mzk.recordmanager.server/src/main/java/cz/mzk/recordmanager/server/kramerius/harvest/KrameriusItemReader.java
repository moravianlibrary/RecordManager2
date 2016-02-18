package cz.mzk.recordmanager.server.kramerius.harvest;

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

import com.google.common.collect.Iterables;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.KrameriusConfiguration;
import cz.mzk.recordmanager.server.oai.dao.KrameriusConfigurationDAO;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;

@Component
@StepScope
public class KrameriusItemReader implements ItemReader<List<HarvestedRecord>>,
		ItemStream, StepExecutionListener {

	@Autowired
	private KrameriusConfigurationDAO configDao;

	@Autowired
	private KrameriusHarvesterFactory harvesterFactory;

	@Autowired
	private TransactionTemplate template;

	@Autowired
	private HibernateSessionSynchronizer sync;

	@Autowired
	private HibernateSessionSynchronizer hibernateSync;

	private KrameriusHarvester kHarvester;
	private KrameriusConfiguration conf;

	// configuration
	private Long confId;

	private Date fromDate;
	private Date untilDate;

	private String nextPid = null;

	private boolean finished = false;

	public KrameriusItemReader(Long confId, Date fromDate, Date untilDate) {
		super();
		this.confId = confId;
		this.fromDate = fromDate;
		this.untilDate = untilDate;
	}

	@Override
	public List<HarvestedRecord> read() {
		if (finished) {
			return null;
		}

		// get uuids
		List<String> uuids = kHarvester.getUuids(nextPid);
		String previousPid = nextPid;
		nextPid = Iterables.getLast(uuids, null);
		
		// get metadata
		List<HarvestedRecord> records = kHarvester.getRecords(uuids);

		if (uuids.isEmpty() || (previousPid != null && previousPid.equals(nextPid))) {
			finished = true;
		}
		
		// return metadata
		return records;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		try (SessionBinder sess = hibernateSync.register()) {
			conf = configDao.get(confId);
			KrameriusHarvesterParams params = new KrameriusHarvesterParams();
			params.setUrl(conf.getUrl());
			params.setMetadataStream(conf.getMetadataStream());
			params.setQueryRows(conf.getQueryRows());
			params.setFrom(fromDate);
			params.setUntil(untilDate);
			kHarvester = harvesterFactory.create(params, confId);
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

	@Override
	public void open(ExecutionContext ctx) throws ItemStreamException {
		if (ctx.containsKey("nextPid")) {
			nextPid = ctx.getString("nextPid");
		}
	}

	@Override
	public void update(ExecutionContext ctx) throws ItemStreamException {
		ctx.putString("nextPid", nextPid);
	}

	@Override
	public void close() throws ItemStreamException {
	}

}
