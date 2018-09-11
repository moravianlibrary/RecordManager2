package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.Oclc;

import java.util.List;

public interface OclcDAO extends DomainDAO<Long, Oclc> {

	List<HarvestedRecord> findHrByOclc(String oclc);

}
