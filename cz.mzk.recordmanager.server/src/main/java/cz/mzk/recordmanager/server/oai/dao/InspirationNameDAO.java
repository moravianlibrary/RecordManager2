package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.imports.inspirations.InspirationType;
import cz.mzk.recordmanager.server.model.InspirationName;

public interface InspirationNameDAO extends DomainDAO<Long, InspirationName> {

	InspirationName getOrCreate(String name, InspirationType type);

}
