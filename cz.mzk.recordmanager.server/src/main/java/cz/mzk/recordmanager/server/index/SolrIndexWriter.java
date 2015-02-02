package cz.mzk.recordmanager.server.index;

import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;

public class SolrIndexWriter implements ItemWriter<SolrInputDocument>, StepExecutionListener {

	private SolrServer server;
	
	@SuppressWarnings("unchecked")
	@Override
	public void write(List<? extends SolrInputDocument> documents) throws Exception {
		server.add((List<SolrInputDocument>) documents);
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		String solrServerUrl = stepExecution.getJobParameters().getString("solrUrl");
		if (solrServerUrl == null) {
			throw new IllegalStateException("Missing job parameter: solrUrl");
		}
		server = new HttpSolrServer(solrServerUrl);
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

}
