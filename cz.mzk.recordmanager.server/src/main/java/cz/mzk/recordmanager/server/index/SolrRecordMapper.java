package cz.mzk.recordmanager.server.index;

import java.util.List;
import java.util.Map;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

public interface SolrRecordMapper {

	List<String> getSupportedFormats();

	Map<String, Object> map(DedupRecord record, List<HarvestedRecord> records);

	Map<String, Object> map(HarvestedRecord record);

}
