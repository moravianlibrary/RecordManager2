package cz.mzk.recordmanager.server.kramerius.harvest;

import cz.mzk.recordmanager.server.kramerius.harvest.KrameriusHarvesterParams;

public interface KrameriusHarvesterFactory {

	public KrameriusHarvester create(KrameriusHarvesterParams parameters,
			Long confId);

	public KrameriusHarvesterNoSorting createNoSorting(KrameriusHarvesterParams parameters,
			Long confId);
	
}
