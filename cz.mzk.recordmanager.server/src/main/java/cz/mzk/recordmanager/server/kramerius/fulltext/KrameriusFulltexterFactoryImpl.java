package cz.mzk.recordmanager.server.kramerius.fulltext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.model.KrameriusConfiguration;

@Component
public class KrameriusFulltexterFactoryImpl implements KrameriusFulltexterFactory {

	@Autowired
	private ApplicationContext appCtx;

	@Override
	public KrameriusFulltexter create(KrameriusConfiguration config) {
		KrameriusFulltexter fulltexter = new KrameriusFulltexterImpl(config.getUrl(), 
				config.getAuthToken(), config.isDownloadPrivateFulltexts());
		AutowireCapableBeanFactory factory = appCtx.getAutowireCapableBeanFactory();
		factory.autowireBean(fulltexter);
		factory.initializeBean(fulltexter, "fulltexter");
		return fulltexter;
	}

}
