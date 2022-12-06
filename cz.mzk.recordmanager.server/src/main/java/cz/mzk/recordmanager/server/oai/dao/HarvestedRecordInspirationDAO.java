package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordInspiration;
import cz.mzk.recordmanager.server.model.Inspiration;

import java.util.List;

public interface HarvestedRecordInspirationDAO extends DomainDAO<Long, HarvestedRecordInspiration> {

	List<HarvestedRecordInspiration> findByName(String name);

	List<HarvestedRecord> fingHrByInspiraion(String name);

	void updateOrCreate(String prefix, String recordId, Inspiration inspiration);

	HarvestedRecordInspiration findByHrIdAndName(Long id, String name);

	HarvestedRecordInspiration find(HarvestedRecord hr, Inspiration inspiration);

}
