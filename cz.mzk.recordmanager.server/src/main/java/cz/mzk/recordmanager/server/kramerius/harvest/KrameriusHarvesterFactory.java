package cz.mzk.recordmanager.server.kramerius.harvest;

import cz.mzk.recordmanager.server.oai.harvest.OAIHarvesterParams;

public interface KrameriusHarvesterFactory {
	
	public KrameriusHarvester create(OAIHarvesterParams parameters,  Long confId) ;

}
