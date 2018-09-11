package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.Cnb;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

import java.util.List;

public interface CnbDAO extends DomainDAO<Long, Cnb> {

	List<HarvestedRecord> findHrByCnb(String cnb);

}
