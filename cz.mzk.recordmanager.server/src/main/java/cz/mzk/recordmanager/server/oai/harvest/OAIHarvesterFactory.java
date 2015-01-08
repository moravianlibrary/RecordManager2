package cz.mzk.recordmanager.server.oai.harvest;

public interface OAIHarvesterFactory {
	
	public OAIHarvester create(OAIHarvesterParams parameters);

}
