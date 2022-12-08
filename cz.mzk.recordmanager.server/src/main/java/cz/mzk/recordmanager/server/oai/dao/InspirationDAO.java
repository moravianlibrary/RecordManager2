package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.imports.inspirations.InspirationType;
import cz.mzk.recordmanager.server.model.Inspiration;

import java.io.IOException;

public interface InspirationDAO extends DomainDAO<Long, Inspiration> {

	Inspiration getOrCreate(String name, InspirationType type) throws IOException;

}
