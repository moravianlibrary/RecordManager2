package cz.mzk.recordmanager.server.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.solr.FaultTolerantIndexingExceptionHandler;
import cz.mzk.recordmanager.server.solr.SolrServerFacade;
import cz.mzk.recordmanager.server.solr.SolrServerFactory;
import cz.mzk.recordmanager.server.util.SolrUtils;

public class SolrIndexWriter implements ItemWriter<Future<List<SolrInputDocument>>>, StepExecutionListener {

	private static Logger logger = LoggerFactory.getLogger(SolrIndexWriter.class);

	@Autowired
	private SolrServerFactory factory;

	private SolrServerFacade server;

	private String solrUrl;

	private int commitWithinMs = 100000;  //<MJ.>

	public SolrIndexWriter(String solrUrl) {
		this.solrUrl = solrUrl;
	}

	@Override
	public void write(List<? extends Future<List<SolrInputDocument>>> items)
			throws Exception {
		List<SolrInputDocument> documents = new ArrayList<SolrInputDocument>();
		for (Future<List<SolrInputDocument>> item : items) {
			List<SolrInputDocument> docs = item.get();
			if (docs != null) {
				docs = SolrUtils.removeHiddenFields(docs);
				documents.addAll(docs);
			}
		}
		if (documents.isEmpty()) {
			return;
		}
		logger.info("About to index {} documents to Solr", documents.size());
		server.add(documents, commitWithinMs);
	}

	public int getCommitWithinMs() {
		return commitWithinMs;
	}

	public void setCommitWithinMs(int commitWithinMs) {
		this.commitWithinMs = commitWithinMs;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		server = factory.create(solrUrl, null, new FaultTolerantIndexingExceptionHandler());
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
