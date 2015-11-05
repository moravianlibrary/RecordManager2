package cz.mzk.recordmanager.server.kramerius.fulltext;

import cz.mzk.recordmanager.server.model.KrameriusConfiguration;

public interface KrameriusFulltexterFactory {

	public KrameriusFulltexter create(KrameriusConfiguration config);

}
