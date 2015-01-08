package cz.mzk.recordmanager.server.oai.harvest;

import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.util.HttpClient;

public class OAIHarvesterFactoryImpl implements OAIHarvesterFactory {

	@Autowired
	private HttpClient httpClient;
	
	@Override
	public OAIHarvester create(OAIHarvesterParams parameters) {
		return new OAIHarvester(httpClient, parameters);
	}

}
