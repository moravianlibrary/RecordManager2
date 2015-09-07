package cz.mzk.recordmanager.server.index.enrich;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;

public class SfxEnricher implements DedupRecordEnricher {

	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		
		Collection<Object> sfxLinks = new HashSet<>();
		localRecords.stream().forEach(doc -> sfxLinks.addAll(doc.getFieldValues(SolrFieldConstants.SFX_LINKS_FIELD)));
		
		mergedDocument.remove(SolrFieldConstants.SFX_LINKS_FIELD);
		mergedDocument.addField(SolrFieldConstants.SFX_LINKS_FIELD, sfxLinks);
		
		localRecords.stream().forEach(doc -> doc.remove(SolrFieldConstants.SFX_LINKS_FIELD));

	}

}
