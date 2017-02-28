package cz.mzk.recordmanager.server.index;

import java.util.List;
import java.util.Map;

import cz.mzk.recordmanager.server.model.AdresarKnihoven;
import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

public interface SolrRecordMapper {

	public List<String> getSupportedFormats();

	public Map<String, Object> map(DedupRecord record, List<HarvestedRecord> records);

	public Map<String, Object> map(HarvestedRecord record);
	
	public Map<String, Object> map(AdresarKnihoven record);

}
