package cz.mzk.recordmanager.server.kramerius.harvest;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.KrameriusConfiguration;
import cz.mzk.recordmanager.server.oai.dao.KrameriusConfigurationDAO;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;
import org.apache.solr.client.solrj.SolrServerException;
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

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Component
@StepScope
public class KrameriusItemReader implements ItemReader<List<HarvestedRecord>>,
		ItemStream, StepExecutionListener {

	@Autowired
	private KrameriusConfigurationDAO configDao;

	@Autowired
	private KrameriusHarvesterFactory harvesterFactory;

	@Autowired
	private HibernateSessionSynchronizer hibernateSync;

	private IKrameriusHarvester kHarvester;

	// configuration
	private Long confId;

	private Date fromDate;
	private Date untilDate;
	private String type;

	public KrameriusItemReader(Long confId, Date fromDate, Date untilDate, String type) {
		super();
		this.confId = confId;
		this.fromDate = fromDate;
		this.untilDate = untilDate;
		this.type = type;
	}

	@Override
	public List<HarvestedRecord> read() throws SolrServerException, IOException {
		// get uuids
		List<String> uuids = kHarvester.getNextUuids();
		if (uuids == null) return null;
		// return metadata
		return kHarvester.getRecords(uuids);
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		try (SessionBinder sess = hibernateSync.register()) {
			KrameriusConfiguration conf = configDao.get(confId);
			if (conf == null) {
				throw new IllegalArgumentException(String.format(
						"Kramerius harvest configuration with id=%s not found", confId));
			}
			KrameriusHarvesterParams params = new KrameriusHarvesterParams();
			params.setUrl(conf.getUrl());
			params.setMetadataStream(conf.getMetadataStream());
			params.setQueryRows(conf.getQueryRows());
			params.setFrom(fromDate);
			params.setUntil(untilDate);
			kHarvester = harvesterFactory.create(type == null ? "" : type, params, confId);
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

	@Override
	public void open(ExecutionContext ctx) throws ItemStreamException {
		if (ctx.containsKey("nextPid")) {
			kHarvester.setLastPid(ctx.getString("nextPid"));
		}
		if (ctx.containsKey("start")) {
			kHarvester.setStart(ctx.getInt("start"));
		}
	}

	@Override
	public void update(ExecutionContext ctx) throws ItemStreamException {
		ctx.putString("nextPid", kHarvester.getLastPid());
		ctx.putInt("start", kHarvester.getStart());
	}

	@Override
	public void close() throws ItemStreamException {
	}

}
