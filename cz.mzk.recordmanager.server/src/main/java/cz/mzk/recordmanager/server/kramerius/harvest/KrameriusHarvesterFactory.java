package cz.mzk.recordmanager.server.kramerius.harvest;

public interface KrameriusHarvesterFactory {

	IKrameriusHarvester create(String type, KrameriusHarvesterParams parameters,
							   Long confId);

}
