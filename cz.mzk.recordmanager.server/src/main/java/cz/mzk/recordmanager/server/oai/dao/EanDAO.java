package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.Ean;
import cz.mzk.recordmanager.server.model.HarvestedRecord;

import java.util.List;

public interface EanDAO extends DomainDAO<Long, Ean> {

	List<HarvestedRecord> findHrByEan(String ean);

}
