package cz.mzk.recordmanager.server.index.enrich;

import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;

@Component
public class LoanRelevanceEnricher implements DedupRecordEnricher{

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		Long count = 0L;
		
		Object recordFormat = mergedDocument.getFieldValue(SolrFieldConstants.RECORD_FORMAT);
		if(recordFormat != null && !recordFormat.toString().contains(HarvestedRecordFormatEnum.PERIODICALS.name())){
			for(SolrInputDocument localRecord: localRecords){
				if(localRecord.getFieldValue(SolrFieldConstants.LOAN_RELEVANCE_FIELD) != null){
					try{
						count += Long.valueOf(localRecord.getFieldValue(SolrFieldConstants.LOAN_RELEVANCE_FIELD).toString());
					}
					catch(NumberFormatException nfe){
					}
				}
			}
			mergedDocument.addField(SolrFieldConstants.LOAN_RELEVANCE_FIELD, count);
		}

		localRecords.forEach(rec -> rec.remove(SolrFieldConstants.LOAN_RELEVANCE_FIELD));
	}

}
