package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.AntikvariatyRecord;
import cz.mzk.recordmanager.server.model.DedupRecord;

public interface AntikvariatyRecordDAO extends DomainDAO<Long, AntikvariatyRecord> {

	/**
	 * Get URL to antikvariaty from DedupRecord
	 *
	 * @param dr {@link DedupRecord}
	 * @return {@link String} URL to Antikvariaty
	 */
	String getLinkToAntikvariaty(DedupRecord dr);

}
