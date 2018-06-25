package cz.mzk.recordmanager.server.kramerius.harvest;

public interface KrameriusHarvesterFactory {

	KrameriusHarvester create(String type, KrameriusHarvesterParams parameters,
							  Long confId);

}
