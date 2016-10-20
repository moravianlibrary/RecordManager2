package cz.mzk.recordmanager.server.index;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.solr.LoggingSolrIndexingExceptionHandler;
import cz.mzk.recordmanager.server.solr.SolrServerFacade;
import cz.mzk.recordmanager.server.solr.SolrServerFactory;
import cz.mzk.recordmanager.server.util.SolrUtils;

public class IndexIndividualHarvestedRecordsTasklet implements Tasklet {

	@Autowired
	private SolrServerFactory solrServerFactory;

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	@Autowired
	private SolrInputDocumentFactory solrInputDocumentFactory;

	private final String solrUrl;

	private final List<String> recordIds;

	private int commitWithinMs = 10000;

	public IndexIndividualHarvestedRecordsTasklet(String solrUrl, List<String> recordIds) {
		super();
		this.solrUrl = solrUrl;
		this.recordIds = recordIds;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		SolrServerFacade solrServer = solrServerFactory.create(solrUrl, null, LoggingSolrIndexingExceptionHandler.INSTANCE);
		List<SolrInputDocument> documents = new ArrayList<SolrInputDocument>(recordIds.size());
		for (String solrId : recordIds) {
			HarvestedRecord rec = harvestedRecordDao.findBySolrId(solrId);
			if (rec == null) {
				throw new IllegalArgumentException(String.format("Harvested record %s not found", solrId));
			}
			SolrInputDocument document = solrInputDocumentFactory.create(rec);
			documents.add(SolrUtils.removeHiddenFields(document));
		}
		solrServer.add(documents, commitWithinMs);
		solrServer.commit();
		return RepeatStatus.FINISHED;
	}

}
