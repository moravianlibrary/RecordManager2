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

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.KrameriusConfiguration;
import cz.mzk.recordmanager.server.oai.dao.KrameriusConfigurationDAO;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;

@Component
@StepScope
public class KrameriusItemReaderNoSorting implements ItemReader<List<HarvestedRecord>>,
		StepExecutionListener, ItemStream {

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

	private KrameriusHarvesterNoSorting kHarvester;
	private KrameriusConfiguration conf;

	// configuration
	private Long confId;

	private Date fromDate;
	private Date untilDate;

	private int start = 0;

	private boolean finished = false;

	public KrameriusItemReaderNoSorting(Long confId, Date fromDate, Date untilDate) {
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
		List<String> uuids = kHarvester.getUuids(start);

		// get metadata
		List<HarvestedRecord> records = kHarvester.getRecords(uuids);

		Long queryRows = conf.getQueryRows();
		if (start < kHarvester.getNumFound()) {
			start += queryRows.intValue();
		} else {
			finished = true;
		}
		
		// return metadata
		return records;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		try (SessionBinder sess = hibernateSync.register()) {
			conf = configDao.get(confId);
			if (confId == null) {
				throw new IllegalArgumentException(String.format(
						"Kramerius harvest configuration with id=%s not found", confId));
			}
			KrameriusHarvesterParams params = new KrameriusHarvesterParams();
			params.setUrl(conf.getUrl());
			params.setMetadataStream(conf.getMetadataStream());
			params.setQueryRows(conf.getQueryRows());
			params.setFrom(fromDate);
			params.setUntil(untilDate);
			kHarvester = harvesterFactory.createNoSorting(params, confId);
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

	@Override
	public void open(ExecutionContext ctx) throws ItemStreamException {
		if (ctx.containsKey("start")) {
			start = ctx.getInt("start");
		}
	}

	@Override
	public void update(ExecutionContext ctx) throws ItemStreamException {
		ctx.putInt("start", start);
	}

	@Override
	public void close() throws ItemStreamException {
	}

}