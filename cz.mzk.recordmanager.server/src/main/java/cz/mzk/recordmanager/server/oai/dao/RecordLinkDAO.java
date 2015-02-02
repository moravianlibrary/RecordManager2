package cz.mzk.recordmanager.server.oai.dao;

import java.util.List;

import cz.mzk.recordmanager.server.model.DedupRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.RecordLink;
import cz.mzk.recordmanager.server.model.RecordLink.RecordLinkId;

public interface RecordLinkDAO extends DomainDAO<RecordLinkId, RecordLink> {
	
	public RecordLink findByHarvestedRecord(HarvestedRecord record);
	
	public List<HarvestedRecord> getHarvestedRecords(DedupRecord master);

}
