package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.api.model.query.ObalkyKnihTOCQuery;
import cz.mzk.recordmanager.server.model.ObalkyKnihAnotation;

import java.util.List;

public interface ObalkyKnihAnotationDAO extends DomainDAO<Long, ObalkyKnihAnotation> {

	List<ObalkyKnihAnotation> findByExample(ObalkyKnihAnotation example, boolean includeNullProperties, String... excludeProperties);

	List<ObalkyKnihAnotation> findByIdentifiers(ObalkyKnihTOCQuery query);

}
