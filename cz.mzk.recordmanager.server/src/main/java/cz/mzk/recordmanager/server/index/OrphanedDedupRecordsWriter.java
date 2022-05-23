package cz.mzk.recordmanager.server.index;

import cz.mzk.recordmanager.server.solr.FaultTolerantIndexingExceptionHandler;
import cz.mzk.recordmanager.server.solr.SolrServerFacade;
import cz.mzk.recordmanager.server.solr.SolrServerFactory;
import cz.mzk.recordmanager.server.util.ProgressLogger;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class OrphanedDedupRecordsWriter implements ItemWriter<Long>, StepExecutionListener {

	@Autowired
	private SolrServerFactory factory;

	private static Logger logger = LoggerFactory.getLogger(OrphanedDedupRecordsWriter.class);

	private ProgressLogger progressLogger;

	private SolrServerFacade server;

	private String solrUrl;

	private int commitWithinMs = 10000;

	public OrphanedDedupRecordsWriter(String solrUrl) {
		this.solrUrl = solrUrl;
	}

	@Override
	public void write(List<? extends Long> items) throws Exception {
		server.deleteById(items.stream().map(i -> i.toString()).collect(Collectors.toList()));
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		server = factory.create(solrUrl, null, new FaultTolerantIndexingExceptionHandler());
		progressLogger = new ProgressLogger(logger, 10000);
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		try {
			logger.info("Final commit");
			server.commit();
		} catch (SolrServerException | IOException ex) {
			throw new RuntimeException("Final commit failed", ex);
		}
		return null;
	}

	public int getCommitWithinMs() {
		return commitWithinMs;
	}

	public void setCommitWithinMs(int commitWithinMs) {
		this.commitWithinMs = commitWithinMs;
	}

}
