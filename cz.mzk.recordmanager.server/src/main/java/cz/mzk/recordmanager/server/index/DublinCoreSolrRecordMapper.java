package cz.mzk.recordmanager.server.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;

@Component
public class DublinCoreSolrRecordMapper implements SolrRecordMapper {

	private final static String FORMAT = "dublinCore";

	@Override
	public List<String> getSupportedFormats() {
		return Collections.singletonList(FORMAT);
	}

	@Override
	public SolrInputDocument map(DedupRecord dedupRecord,
			List<HarvestedRecord> records) {
		if (records.isEmpty()) {
			return null;
		}
		HarvestedRecord record = records.get(0);
		SolrInputDocument document = new SolrInputDocument();
		document.addField(SolrFieldConstants.ID_FIELD, dedupRecord.getId());
		document.addField(SolrFieldConstants.MERGED_FIELD, 1);
		document.addField(SolrFieldConstants.WEIGHT, record.getWeight());
		List<String> localIds = new ArrayList<String>();
		for (HarvestedRecord rec : records) {
			localIds.add(getId(rec));
		}
		document.addField(SolrFieldConstants.LOCAL_IDS_FIELD, localIds);
		return document;
	}

	@Override
	public SolrInputDocument map(HarvestedRecord record) {
		SolrInputDocument document = new SolrInputDocument();
		String id = getId(record);
		document.addField(SolrFieldConstants.ID_FIELD, id);
		document.addField(SolrFieldConstants.INSTITUTION_FIELD, getInstitutionOfRecord(record));
		document.addField(SolrFieldConstants.MERGED_CHILD_FIELD, 1);
		document.addField(SolrFieldConstants.WEIGHT, record.getWeight());
		return document;
	}

	protected String getId(HarvestedRecord record) {
		String prefix = record.getHarvestedFrom().getIdPrefix();
		String id = ((prefix != null) ? prefix + "." : "") + record.getUniqueId().getRecordId();
		return id;
	}

	protected String getInstitutionOfRecord(HarvestedRecord hr) {
		OAIHarvestConfiguration config = hr.getHarvestedFrom();
		if (config != null
				&& config.getLibrary() != null
				&& config.getLibrary().getName() != null) {
			return config.getLibrary().getName();
		}
		return SolrFieldConstants.UNKNOWN_INSTITUTION;
	}

}
