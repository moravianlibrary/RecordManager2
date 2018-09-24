package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.api.model.query.ObalkyKnihTOCQuery;
import cz.mzk.recordmanager.server.model.ObalkyKnihAnnotation;

import java.util.List;

public interface ObalkyKnihAnnotationDAO extends DomainDAO<Long, ObalkyKnihAnnotation> {

	List<ObalkyKnihAnnotation> findByExample(ObalkyKnihAnnotation example, boolean includeNullProperties, String... excludeProperties);

	List<ObalkyKnihAnnotation> findByIdentifiers(ObalkyKnihTOCQuery query);

}
