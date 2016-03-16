package cz.mzk.recordmanager.server.kramerius.fulltext;

import java.io.IOException;
import java.util.List;

import cz.mzk.recordmanager.server.model.FulltextKramerius;

public interface KrameriusFulltexter {

	public List<FulltextKramerius> getFulltextObjects(String rootUuid) throws IOException;

	public List<FulltextKramerius> getFulltextForRoot(String rootUuid) throws IOException;

}
