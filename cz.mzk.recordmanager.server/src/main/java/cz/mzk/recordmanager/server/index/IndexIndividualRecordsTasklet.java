package cz.mzk.recordmanager.server.index;

import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.ImportConfigurationDAO;
import cz.mzk.recordmanager.server.solr.LoggingSolrIndexingExceptionHandler;
import cz.mzk.recordmanager.server.solr.SolrServerFacade;
import cz.mzk.recordmanager.server.solr.SolrServerFactory;

public class IndexIndividualRecordsTasklet implements Tasklet {

	@Autowired
	private SolrServerFactory solrServerFactory;

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	@Autowired
	private ImportConfigurationDAO importConfigurationDao;

	@Autowired
	private SolrInputDocumentFactory solrInputDocumentFactory;

	private final String solrUrl;

	private final List<String> recordIds;

	private int commitWithinMs = 10000;

	public IndexIndividualRecordsTasklet(String solrUrl, List<String> recordIds) {
		super();
		this.solrUrl = solrUrl;
		this.recordIds = recordIds;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		SolrServerFacade solrServer = solrServerFactory.create(solrUrl, LoggingSolrIndexingExceptionHandler.INSTANCE);
		for (String recordIdWithPrefix : recordIds) {
			String[] components = recordIdWithPrefix.split("\\.", 2);
			String prefix = components[0];
			String recordId = components[1];
			ImportConfiguration configuration = importConfigurationDao.findByIdPrefix(prefix);
			if (configuration == null) {
				throw new IllegalArgumentException(String.format("Import configuration with prefix %s not found", prefix));
			}
			HarvestedRecord rec = harvestedRecordDao.findByIdAndHarvestConfiguration(recordId, configuration);
			if (rec == null) {
				throw new IllegalArgumentException(String.format("Harvested record %s not found", recordIdWithPrefix));
			}
			DedupRecord dedupRecord = rec.getDedupRecord();
			if (dedupRecord == null) {
				throw new IllegalArgumentException(String.format("Harvested record %s is not deduplicated, run dedup first.", recordIdWithPrefix));
			}
			List<HarvestedRecord> records = harvestedRecordDao.getByDedupRecord(dedupRecord);
			List<SolrInputDocument> documents = solrInputDocumentFactory.create(dedupRecord, records);
			solrServer.add(documents, commitWithinMs);
		}
		solrServer.commit();
		return RepeatStatus.FINISHED;
	}

}
