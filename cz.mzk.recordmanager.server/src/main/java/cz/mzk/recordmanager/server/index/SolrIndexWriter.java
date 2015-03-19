package cz.mzk.recordmanager.server.index;

import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

public class SolrIndexWriter implements ItemWriter<SolrInputDocument>, StepExecutionListener {

	@Autowired
	private SolrServerFactory factory;
	
	private String solrUrl;

	private int commitWithinMs = 10000;

	private SolrServer server;
	
	public SolrIndexWriter(String solrUrl) {
		this.solrUrl = solrUrl;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void write(List<? extends SolrInputDocument> documents) throws Exception {
		UpdateResponse response = server.add((List<SolrInputDocument>) documents, commitWithinMs);
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
