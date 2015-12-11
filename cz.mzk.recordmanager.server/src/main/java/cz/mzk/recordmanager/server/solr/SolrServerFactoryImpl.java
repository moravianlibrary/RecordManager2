package cz.mzk.recordmanager.server.solr;

import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;

public class SolrServerFactoryImpl implements SolrServerFactory {

	public static enum Mode {

		DEFAULT {
			@Override
			public SolrServer create(String url) {
				HttpSolrServer solr = new HttpSolrServer(url);
				return solr;
			}
		},

		KRAMERIUS {
			@Override
			public SolrServer create(String url) {
				HttpClient client  = new KrameriusHttpClient();
				HttpSolrServer solr = new HttpSolrServer(url, client);
				solr.setParser(new XMLResponseParser());
				return solr;
			}
		};

		public abstract SolrServer create(String url);

	}

	@Override
	public SolrServerFacade create(String url, Mode mode, SolrIndexingExceptionHandler exceptionHandler) {
		if (mode == null) {
			mode = Mode.DEFAULT;
		}
		SolrServer server = mode.create(url);
		return new SolrServerFacadeImpl(server, exceptionHandler);
	}

	@Override
	public SolrServerFacade create(String url) {
		return create(url, Mode.DEFAULT, RethrowingSolrIndexingExceptionHandler.INSTANCE);
	}

	@Override
	public SolrServerFacade create(String url, Mode mode) {
		return create(url, mode, RethrowingSolrIndexingExceptionHandler.INSTANCE);
	}

}
