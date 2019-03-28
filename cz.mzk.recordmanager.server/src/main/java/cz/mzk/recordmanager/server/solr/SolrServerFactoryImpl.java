package cz.mzk.recordmanager.server.solr;

import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public class SolrServerFactoryImpl implements SolrServerFactory {

	private static Logger logger = LoggerFactory.getLogger(SolrServerFactoryImpl.class);

	public enum Mode {

		DEFAULT {

			@Override
			public HttpSolrClient create(String url) {
				HttpSolrClient solr = new HttpSolrClient.Builder(url).build();
				solr.setParser(new BinaryResponseParser());
				solr.setRequestWriter(new BinaryRequestWriter());
				return solr;
			}

		},

		DEFAULT_XML {

			@Override
			public HttpSolrClient create(String url) {
				HttpSolrClient solr = new HttpSolrClient.Builder(url).build();
				solr.setParser(new XMLResponseParser());
				return solr;
			}

		},

		KRAMERIUS_DIRECT {

			@Override
			public HttpSolrClient create(String url) {
				HttpClient client  = new KrameriusHttpClient();
				HttpSolrClient solr = new HttpSolrClient.Builder(url).withHttpClient(client).build();
				solr.setParser(new XMLResponseParser());
				return solr;
			}

		},

		KRAMERIUS {

			private final String REQUEST_PATH = "/search";

			@Override
			public HttpSolrClient create(String url) {
				HttpClient client  = new KrameriusHttpClient();
				HttpSolrClient solr = new HttpSolrClient.Builder(url).withHttpClient(client).build();
				solr.setParser(new XMLResponseParser());
				return solr;
			}

			@Override
			public String getRequestPath() {
				return REQUEST_PATH;
			}

		};

		public abstract HttpSolrClient create(String url);

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
		HttpSolrClient server = mode.create(url);
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
