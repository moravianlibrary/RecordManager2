package cz.mzk.recordmanager.server.kramerius.harvest;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
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
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
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

	private Integer start;

	private boolean finished = false;

	public KrameriusItemReader(Long confId, Date fromDate, Date untilDate) {
		super();
		this.confId = confId;
		this.fromDate = fromDate;
		this.untilDate = untilDate;

		start = 0;
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

		for (HarvestedRecord r : records) {

//			String s;
//			try {
//				s = new String(r.getRawRecord(), "UTF-8");
//			} catch (UnsupportedEncodingException e) {
//				e.printStackTrace();
//			}
//			System.out.println(s);
		}

		// decide if continue (docNum vs start)

//		System.out.println("porovnavame start: " + start
//				+ " a harvester.numFound():" + kHarvester.getNumFound());
		Long queryRows = conf.getQueryRows();
		if (start < kHarvester.getNumFound()) {
			start = start + queryRows.intValue();
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
			KrameriusHarvesterParams params = new KrameriusHarvesterParams();
			params.setUrl(conf.getUrl());
			params.setMetadataStream(conf.getMetadataStream());
			params.setQueryRows(conf.getQueryRows());
			params.setModel(conf.getModel());
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
	}

	@Override
	public void update(ExecutionContext ctx) throws ItemStreamException {
	}

	@Override
	public void close() throws ItemStreamException {
	}

}
