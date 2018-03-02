package cz.mzk.recordmanager.server.oai.harvest;

public interface OAIHarvesterFactory {
	
	OAIHarvester create(OAIHarvesterParams parameters);

}
