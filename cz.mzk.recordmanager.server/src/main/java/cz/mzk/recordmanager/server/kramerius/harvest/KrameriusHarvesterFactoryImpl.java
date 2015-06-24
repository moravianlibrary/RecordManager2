package cz.mzk.recordmanager.server.kramerius.harvest;

import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.oai.harvest.OAIHarvesterParams;
import cz.mzk.recordmanager.server.util.HttpClient;

public class KrameriusHarvesterFactoryImpl implements KrameriusHarvesterFactory {

	@Autowired
	private HttpClient httpClient;

	@Override
	public KrameriusHarvester create(KrameriusHarvesterParams parameters,
			Long confId) {
		return new KrameriusHarvester(httpClient, parameters, confId);
	}

}
