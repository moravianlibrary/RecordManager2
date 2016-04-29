package cz.mzk.recordmanager.server.solr;

import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.BinaryResponseParser;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public class SolrServerFactoryImpl implements SolrServerFactory {

	private static Logger logger = LoggerFactory.getLogger(SolrServerFactoryImpl.class);

	public static enum Mode {

		DEFAULT {

			@Override
			public SolrServer create(String url) {
				HttpSolrServer solr = new HttpSolrServer(url);
				solr.setParser(new BinaryResponseParser());
				solr.setRequestWriter(new BinaryRequestWriter());
				return solr;
			}

		},

		DEFAULT_XML {

			@Override
			public SolrServer create(String url) {
				HttpSolrServer solr = new HttpSolrServer(url);
				solr.setParser(new XMLResponseParser());
				return solr;
			}

		},

		KRAMERIUS_DIRECT {

			@Override
			public SolrServer create(String url) {
				HttpClient client  = new KrameriusHttpClient();
				HttpSolrServer solr = new HttpSolrServer(url, client);
				solr.setParser(new XMLResponseParser());
				return solr;
			}

		},

		KRAMERIUS {

			private final String REQUEST_PATH = "/search";

			@Override
			public SolrServer create(String url) {
				HttpClient client  = new KrameriusHttpClient();
				HttpSolrServer solr = new HttpSolrServer(url, client);
				solr.setParser(new XMLResponseParser());
				return solr;
			}

			@Override
			public String getRequestPath() {
				return REQUEST_PATH;
			}

		};

		public abstract SolrServer create(String url);

		public String getRequestPath() {
			return null;
		}

	}

	@Value(value = "${solr.javabin:#{true}}")
	private boolean javabin = true;

	@Override
	public SolrServerFacade create(String url, Mode mode, SolrIndexingExceptionHandler exceptionHandler) {
		if (mode == null) {
			mode = (javabin) ? Mode.DEFAULT : Mode.DEFAULT_XML;
		}
		logger.info("About to create SolrServerFacade for url: {}", (mode.getRequestPath() == null) ? url : url +  mode.getRequestPath());
		SolrServer server = mode.create(url);
		return new SolrServerFacadeImpl(server, exceptionHandler, mode.getRequestPath());
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
