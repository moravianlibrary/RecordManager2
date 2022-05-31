package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Isbn;

import java.util.List;

public interface IsbnDAO extends DomainDAO<Long, Isbn> {

	List<HarvestedRecord> findHrByIsbn(Long isbn);

	List<byte []> findCaslinHrByIsbn(Long isbn);

}
