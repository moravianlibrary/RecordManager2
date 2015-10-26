package cz.mzk.recordmanager.server.index;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.solr.SolrServerFacade;
import cz.mzk.recordmanager.server.solr.SolrServerFactory;

public class SolrIndexWriter implements ItemWriter<Future<List<SolrInputDocument>>>, StepExecutionListener {

	private static Logger logger = LoggerFactory.getLogger(SolrRecordProcessor.class);

	@Autowired
	private SolrServerFactory factory;

	private SolrServerFacade server;

	private String solrUrl;

	private int commitWithinMs = 10000;

	public SolrIndexWriter(String solrUrl) {
		this.solrUrl = solrUrl;
	}

	@Override
	public void write(List<? extends Future<List<SolrInputDocument>>> items)
			throws Exception {
		try {
			List<SolrInputDocument> documents = new ArrayList<SolrInputDocument>();
			for (Future<List<SolrInputDocument>> item : items) {
				List<SolrInputDocument> docs = item.get();
				if (docs != null) {
					documents.addAll(docs);
				}
			}
			if (documents.isEmpty()) {
				return;
			}
			logger.info("About to index {} documents to Solr", documents.size());
			server.add(documents, commitWithinMs);
		} catch (Exception ex) {
			logger.error("Exception thrown when indexing documents to Solr", ex);
		}
	}

	public int getCommitWithinMs() {
		return commitWithinMs;
	}

	public void setCommitWithinMs(int commitWithinMs) {
		this.commitWithinMs = commitWithinMs;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		server = factory.create(solrUrl);
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

}