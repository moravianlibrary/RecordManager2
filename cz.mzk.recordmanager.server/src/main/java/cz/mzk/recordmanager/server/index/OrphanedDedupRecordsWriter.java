package cz.mzk.recordmanager.server.index;

import java.text.MessageFormat;
import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.solr.SolrServerFacade;
import cz.mzk.recordmanager.server.solr.SolrServerFactory;

public class OrphanedDedupRecordsWriter implements ItemWriter<Long>, StepExecutionListener {

	@Autowired
	private SolrServerFactory factory;

	private SolrServerFacade server;

	private String solrUrl;

	private int commitWithinMs = 10000;

	public OrphanedDedupRecordsWriter(String solrUrl) {
		this.solrUrl = solrUrl;
	}

	@Override
	public void write(List<? extends Long> items) throws Exception {
		for (Long id : items) {
			String query = MessageFormat.format("('{'!child of=merged_boolean:true'}'id:{0}) OR id:{0}", id.toString());
			server.deleteByQuery(query, commitWithinMs);
		}
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		server = factory.create(solrUrl);
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

	public int getCommitWithinMs() {
		return commitWithinMs;
	}

	public void setCommitWithinMs(int commitWithinMs) {
		this.commitWithinMs = commitWithinMs;
	}

}
