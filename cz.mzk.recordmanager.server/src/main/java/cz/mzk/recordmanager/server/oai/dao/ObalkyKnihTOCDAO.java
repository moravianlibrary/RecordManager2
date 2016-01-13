package cz.mzk.recordmanager.server.oai.dao;

import java.util.List;

import cz.mzk.recordmanager.api.model.query.ObalkyKnihTOCQuery;
import cz.mzk.recordmanager.server.model.ObalkyKnihTOC;

public interface ObalkyKnihTOCDAO extends DomainDAO<Long, ObalkyKnihTOC>  {

	public List<ObalkyKnihTOC> findByExample(ObalkyKnihTOC example, boolean includeNullProperties, String... excludeProperties);

	public List<ObalkyKnihTOC> findByIsbn(Long isbn);

	public List<ObalkyKnihTOC> query(ObalkyKnihTOCQuery query);

}
