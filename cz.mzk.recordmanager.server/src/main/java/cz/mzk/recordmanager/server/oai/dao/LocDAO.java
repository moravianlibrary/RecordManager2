package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Loc;

import java.util.List;

public interface LocDAO extends DomainDAO<Long, Loc> {

	List<HarvestedRecord> findHrByLoc(final String loc, final String subfield);

	List<HarvestedRecord> findHrByLoc(final List<String> loc);

}
