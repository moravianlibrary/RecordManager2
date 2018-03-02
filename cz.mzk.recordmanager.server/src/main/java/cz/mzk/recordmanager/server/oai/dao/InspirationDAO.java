package cz.mzk.recordmanager.server.oai.dao;

import java.util.List;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Inspiration;

public interface InspirationDAO extends DomainDAO<Long, Inspiration> {
	
	List<Inspiration> findByName(String name);
	
	List<HarvestedRecord> fingHrByInspiraion(String name);
	
	Inspiration findByHrIdAndName(Long id, String name);
}
