package cz.mzk.recordmanager.server.oai.dao;

import java.util.List;

import cz.mzk.recordmanager.server.model.ObalkyKnihTOC;

public interface ObalkyKnihTOCDAO extends DomainDAO<Long, ObalkyKnihTOC>  {

	public List<ObalkyKnihTOC> findByExample(ObalkyKnihTOC example);

}
