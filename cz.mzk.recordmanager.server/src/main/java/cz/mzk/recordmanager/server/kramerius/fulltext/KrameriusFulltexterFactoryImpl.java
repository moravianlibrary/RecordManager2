package cz.mzk.recordmanager.server.kramerius.fulltext;

import cz.mzk.recordmanager.server.kramerius.harvest.KrameriusHarvesterParams;
import cz.mzk.recordmanager.server.model.KrameriusConfiguration;
import cz.mzk.recordmanager.server.solr.SolrServerFactory;
import cz.mzk.recordmanager.server.solr.SolrServerFactoryImpl.Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class KrameriusFulltexterFactoryImpl implements KrameriusFulltexterFactory {

	@Autowired
	private ApplicationContext appCtx;

	@Autowired
	private SolrServerFactory solrFactory;

	@Override
	public KrameriusFulltexter create(KrameriusConfiguration config) {
		return create(config, null);
	}

	@Override
	public KrameriusFulltexter create(KrameriusConfiguration config, KrameriusHarvesterParams params) {
		KrameriusFulltexter fulltexter = null;
		switch (config.getFulltextHarvestType()) {
		case "solr":
			fulltexter = new KrameriusFulltexterSolr(solrFactory.create(config.getUrlSolr(), Mode.DEFAULT), params);
			break;
		default:
			fulltexter = new KrameriusFulltexterFedora(params);
		}
		init(fulltexter);
		return fulltexter;
	}

	private void init(KrameriusFulltexter fulltexter) {
		AutowireCapableBeanFactory factory = appCtx.getAutowireCapableBeanFactory();
		factory.autowireBean(fulltexter);
		factory.initializeBean(fulltexter, "fulltexter");
	}

}
