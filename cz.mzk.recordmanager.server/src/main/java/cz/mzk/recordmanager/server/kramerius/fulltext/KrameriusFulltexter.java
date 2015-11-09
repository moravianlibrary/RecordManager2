package cz.mzk.recordmanager.server.kramerius.fulltext;

import java.io.IOException;
import java.util.List;

import cz.mzk.recordmanager.server.model.FulltextMonography;

public interface KrameriusFulltexter {

	public List<FulltextMonography> getFulltextObjects(String rootUuid) throws IOException;

}
