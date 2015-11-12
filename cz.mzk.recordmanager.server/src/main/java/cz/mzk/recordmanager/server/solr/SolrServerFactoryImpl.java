package cz.mzk.recordmanager.server.solr;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;

public class SolrServerFactoryImpl implements SolrServerFactory {

	public static enum Mode {

		DEFAULT {
			@Override
			public void init(SolrServer solr) {
			}
		},

		KRAMERIUS {
			@Override
			public void init(SolrServer solr) {
				if (solr instanceof HttpSolrServer) {
					((HttpSolrServer) solr).setParser(new XMLResponseParser());
				}
			}
		};

		public abstract void init(SolrServer solr);

	}

	@Override
	public SolrServerFacade create(String url, Mode mode,SolrIndexingExceptionHandler exceptionHandler) {
		HttpSolrServer server = new HttpSolrServer(url);
		if (mode == null) {
			mode = Mode.DEFAULT;
		}
		mode.init((SolrServer) server);
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
