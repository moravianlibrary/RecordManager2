package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Inspiration;
import cz.mzk.recordmanager.server.model.InspirationName;

import java.util.List;

public interface InspirationDAO extends DomainDAO<Long, Inspiration> {

	List<Inspiration> findByName(String name);

	List<HarvestedRecord> fingHrByInspiraion(String name);

	void updateOrCreate(String prefix, String recordId, InspirationName inspirationName);

	Inspiration findByHrIdAndName(Long id, String name);

	Inspiration find(HarvestedRecord hr, InspirationName inspirationName);

}
