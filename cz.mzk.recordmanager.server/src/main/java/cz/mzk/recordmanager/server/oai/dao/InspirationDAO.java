package cz.mzk.recordmanager.server.oai.dao;

import java.util.List;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Inspiration;

public interface InspirationDAO extends DomainDAO<Long, Inspiration> {
	
	public List<Inspiration> findByName(String name);
	
	public List<HarvestedRecord> fingHrByInspiraion(String name);
	
	public Inspiration findByHrIdAndName(Long id, String name);
}
