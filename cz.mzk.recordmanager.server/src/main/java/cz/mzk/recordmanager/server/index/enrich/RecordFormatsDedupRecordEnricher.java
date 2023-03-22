package cz.mzk.recordmanager.server.index.enrich;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat;
import cz.mzk.recordmanager.server.util.SolrUtils;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class RecordFormatsDedupRecordEnricher implements DedupRecordEnricher {

	private final static List<String> TYPE_OTHER_COMP_CARRIER = SolrUtils.createHierarchicFacetValues(
			"OTHER", "COMPUTER_CARRIER");
	private final static String ONLINE = "0/online/";
	
	/**
	 * remove record format OTHER_COMPUTER_CARRIER if avalability online
	 */
	@Override
	public void enrich(DedupRecord record, SolrInputDocument mergedDocument,
			List<SolrInputDocument> localRecords) {
		boolean online = false;
		for (SolrInputDocument doc : localRecords) {
			Collection<Object> statuses = doc.getFieldValues(SolrFieldConstants.LOCAL_STATUSES_FACET);
			if (statuses != null && statuses.contains(ONLINE)) {
				online = true;
				Collection<Object> formats = doc.getFieldValues(SolrFieldConstants.RECORD_FORMAT_DISPLAY);
				if (formats != null) {
					doc.setField(SolrFieldConstants.RECORD_FORMAT_DISPLAY, removeComputerCarrier(formats));
				}
			}
		}
		
		if (online) {
			Collection<Object> formats = mergedDocument.getFieldValues(SolrFieldConstants.RECORD_FORMAT);
			if (formats != null) {
				mergedDocument.setField(SolrFieldConstants.RECORD_FORMAT, removeComputerCarrier(formats));
				mergedDocument.setField(SolrFieldConstants.RECORD_FORMAT_FACET, removeComputerCarrier(formats));
			}
		}
	}
	
	protected Collection<Object> removeComputerCarrier(Collection<Object> formats) {
		boolean compCarrier = false;
		int otherCount = 0;
		
		for (Object obj : formats) {
			if (TYPE_OTHER_COMP_CARRIER.get(1).equals(obj)) {
				compCarrier = true;
				otherCount++;
			}
			if (obj.toString().startsWith(TYPE_OTHER_COMP_CARRIER.get(0))) otherCount++; 
		}
		if (compCarrier) {
			if (otherCount == 2) formats.removeAll(TYPE_OTHER_COMP_CARRIER);
			else formats.remove(TYPE_OTHER_COMP_CARRIER.get(1));
		}
		if (formats.isEmpty())
			formats.addAll(SolrUtils.createHierarchicFacetValues(
					HarvestedRecordFormat.HarvestedRecordFormatEnum.OTHER_OTHER.name().split("_")));
		return formats;
	}

}
