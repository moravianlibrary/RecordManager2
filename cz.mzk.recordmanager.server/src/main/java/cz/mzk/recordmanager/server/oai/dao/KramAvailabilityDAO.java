package cz.mzk.recordmanager.server.oai.dao;

import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.model.KramAvailability;

import java.util.List;

public interface KramAvailabilityDAO extends DomainDAO<Long, KramAvailability> {

	KramAvailability getByConfigAndUuid(final ImportConfiguration config, final String uuid);

	List<KramAvailability> getByUuid(final String uuid);

	void dropKeys(KramAvailability availability);

}
