package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.api.model.query.ObalkyKnihTOCQuery;
import cz.mzk.recordmanager.server.model.ObalkyKnihTOC;

import java.util.List;

public interface ObalkyKnihTOCDAO extends DomainDAO<Long, ObalkyKnihTOC>  {

	List<ObalkyKnihTOC> findByExample(ObalkyKnihTOC example, boolean includeNullProperties, String... excludeProperties);

	List<ObalkyKnihTOC> findByIsbn(Long isbn);

	List<ObalkyKnihTOC> query(ObalkyKnihTOCQuery query);

	List<ObalkyKnihTOC> findByBookId(Long book_id);

}
