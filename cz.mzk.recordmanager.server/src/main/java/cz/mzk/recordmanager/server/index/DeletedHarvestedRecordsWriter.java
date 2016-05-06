package cz.mzk.recordmanager.server.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.solr.SolrServerFacade;
import cz.mzk.recordmanager.server.solr.SolrServerFactory;
import cz.mzk.recordmanager.server.util.IndexingUtils;

public class DeletedHarvestedRecordsWriter implements ItemWriter<HarvestedRecord>, StepExecutionListener {

	private static Logger logger = LoggerFactory.getLogger(DeletedHarvestedRecordsWriter.class);

	@Autowired
	private SolrServerFactory factory;

	private SolrServerFacade server;

	private String solrUrl;

	public DeletedHarvestedRecordsWriter(String solrUrl) {
		this.solrUrl = solrUrl;
	}

	@Override
	public void write(List<? extends HarvestedRecord> records) throws Exception {
		List<String> ids = new ArrayList<String>(records.size());
		for (HarvestedRecord record : records) {
			ids.add(IndexingUtils.getSolrId(record));
		}
		logger.trace("About to delete: {}", ids);
		server.deleteById(ids);
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		server = factory.create(solrUrl);
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		try {
			server.commit();
		} catch (SolrServerException | IOException ex) {
			throw new RuntimeException("Final commit failed", ex);
		}
		return null;
	}

}
