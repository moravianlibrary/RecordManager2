package cz.mzk.recordmanager.server.oai.harvest;

import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.util.HttpClient;
import cz.mzk.recordmanager.server.util.proxy.FaultTolerantProxy;

public class OAIHarvesterFactoryImpl implements OAIHarvesterFactory {

	@Autowired
	private HttpClient httpClient;
	
	@Override
	public OAIHarvester create(OAIHarvesterParams parameters) {
		OAIHarvester harvester = new OAIHarvesterImpl(httpClient, parameters);
		return FaultTolerantProxy.create(harvester, 3, 1000);
	}

}
