package cz.mzk.recordmanager.server.index;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.solr.SolrServerFacade;
import cz.mzk.recordmanager.server.solr.SolrServerFactory;

public class DeletedHarvestedRecordsWriter implements ItemWriter<String>, StepExecutionListener {

	@Autowired
	private SolrServerFactory factory;

	private SolrServerFacade server;

	private String solrUrl;

	public DeletedHarvestedRecordsWriter(String solrUrl) {
		this.solrUrl = solrUrl;
	}

	@Override
	public void write(List<? extends String> items) throws Exception {
		List<String> ids = new ArrayList<String>(items.size());
		for (String id : items) {
			ids.add(id);
		}
		server.deleteById(ids);
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
