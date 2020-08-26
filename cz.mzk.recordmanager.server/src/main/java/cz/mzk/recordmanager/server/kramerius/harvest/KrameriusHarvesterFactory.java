package cz.mzk.recordmanager.server.kramerius.harvest;

public interface KrameriusHarvesterFactory {

	KrameriusHarvester create(KrameriusHarvesterEnum type, KrameriusHarvesterParams parameters,
			Long confId, String inFile);

}
