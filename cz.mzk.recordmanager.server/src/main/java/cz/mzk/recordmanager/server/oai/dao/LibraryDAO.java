package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.Library;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;

import java.util.List;

public interface LibraryDAO extends DomainDAO<Long, Library> {

    public List<OAIHarvestConfiguration> getOAIHarvestConfigurations(Long libraryId);

    public void updateLibrary(Library library);

    public void updateOaiHarvestConfiguration(OAIHarvestConfiguration config);
}
