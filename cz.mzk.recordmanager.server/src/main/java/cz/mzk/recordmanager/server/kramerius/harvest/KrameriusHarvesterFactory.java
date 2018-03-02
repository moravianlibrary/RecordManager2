package cz.mzk.recordmanager.server.kramerius.harvest;

import cz.mzk.recordmanager.server.kramerius.harvest.KrameriusHarvesterParams;

public interface KrameriusHarvesterFactory {

	KrameriusHarvester create(KrameriusHarvesterParams parameters,
							  Long confId);

	KrameriusHarvesterNoSorting createNoSorting(KrameriusHarvesterParams parameters,
												Long confId);

}
