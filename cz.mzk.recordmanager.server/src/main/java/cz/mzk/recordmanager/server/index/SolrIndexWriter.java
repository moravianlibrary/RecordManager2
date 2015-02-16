package cz.mzk.recordmanager.server.index;

import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
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
	
	private SolrServer server;
	
	public SolrIndexWriter(String solrUrl) {
		this.solrUrl = solrUrl;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void write(List<? extends SolrInputDocument> documents) throws Exception {
		server.add((List<SolrInputDocument>) documents);
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
