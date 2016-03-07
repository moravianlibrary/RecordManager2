package cz.mzk.recordmanager.server.solr;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.mzk.recordmanager.server.index.SolrFieldConstants;
import cz.mzk.recordmanager.server.index.enrich.LazyFulltextFieldImpl;

public class SolrServerFacadeImpl implements SolrServerFacade {

	private static Logger logger = LoggerFactory.getLogger(SolrServerFacadeImpl.class);

	protected final SolrServer server;

	protected final SolrIndexingExceptionHandler exceptionHandler;

	protected final String requestPath;

	public SolrServerFacadeImpl(SolrServer server) {
		this(server, RethrowingSolrIndexingExceptionHandler.INSTANCE);
	}

	public SolrServerFacadeImpl(SolrServer server, SolrIndexingExceptionHandler exceptionHandler) {
		this(server, RethrowingSolrIndexingExceptionHandler.INSTANCE, null);
	}

	public SolrServerFacadeImpl(SolrServer server, SolrIndexingExceptionHandler exceptionHandler, String requestPath) {
		super();
		this.server = server;
		this.exceptionHandler = exceptionHandler;
		this.requestPath = requestPath;
	}

	@Override
	public void add(Collection<SolrInputDocument> documents, int commitWithinMs)
			throws IOException, SolrServerException {
		List<SolrInputDocument> docsWithoutFulltext = documents.stream().filter(doc -> !hasLazyFulltext(doc)).collect(Collectors.toList());
		List<SolrInputDocument> docsWithFulltext = documents.stream().filter(doc -> hasLazyFulltext(doc)).collect(Collectors.toList());
		if (!docsWithoutFulltext.isEmpty()) {
			try {
				server.add(docsWithoutFulltext, commitWithinMs);
			} catch (SolrException | SolrServerException | IOException ex) {
				if (exceptionHandler.handle(ex, documents)) {
					fallbackIndex(docsWithoutFulltext, commitWithinMs);
				}
			}
		}
		if (!docsWithFulltext.isEmpty()) {
			indexLazyFulltext(docsWithFulltext, commitWithinMs);
		}
	}

	@Override
	public void commit() throws SolrServerException, IOException {
		server.commit();
	}

	@Override
	public QueryResponse query(final SolrRequest request) throws SolrServerException, IOException {
		NamedList<Object> req = server.request(request);
		return new QueryResponse(req, server);
	}

	public QueryResponse query(SolrQuery query) throws SolrServerException {
		if (requestPath == null) {
			return server.query(query);
		} else {
			SolrRequest request = new QueryRequest(query);
			request.setPath("/solr");
			NamedList<Object> req;
			try {
				req = server.request(request);
			} catch (IOException ioe) {
				throw new SolrServerException(ioe);
			}
			return new QueryResponse(req, server);
		}
	}

	@Override
	public void deleteById(List<String> ids) throws SolrServerException, IOException {
		server.deleteById(ids);
	}

	@Override
	public void deleteByQuery(String query) throws SolrServerException, IOException {
		server.deleteByQuery(query);
	}

	@Override
	public void deleteByQuery(String query, int commitWithinMs) throws SolrServerException, IOException {
		server.deleteByQuery(query, commitWithinMs);
	}

	private void fallbackIndex(Collection<SolrInputDocument> documents, int commitWithinMs) throws SolrServerException {
		for (SolrInputDocument document : documents) {
			try {
				logger.info("Indexing document with fallbackIndex method!");
				server.add(document, commitWithinMs);
			} catch (SolrException | SolrServerException | IOException ex) {
				exceptionHandler.handle(ex, Collections.singletonList(document));
			}
		}
	}

	private void indexLazyFulltext(Collection<SolrInputDocument> documents, int commitWithinMs) throws SolrServerException {
		for (SolrInputDocument document : documents) {
			try {
				LazyFulltextFieldImpl fulltext = (LazyFulltextFieldImpl) document.getFieldValue(SolrFieldConstants.FULLTEXT_FIELD);
				document.setField(SolrFieldConstants.FULLTEXT_FIELD, fulltext.getContent());
				server.add(document, commitWithinMs);
			} catch (SolrException | SolrServerException | IOException ex) {
				exceptionHandler.handle(ex, Collections.singletonList(document));
			} finally {
				// to enable garbage collection
				document.removeField(SolrFieldConstants.FULLTEXT_FIELD);
			}
		}
	}

	private boolean hasLazyFulltext(SolrInputDocument document) {
		Object fulltext = document.getFieldValue(SolrFieldConstants.FULLTEXT_FIELD);
		return (fulltext != null && fulltext instanceof LazyFulltextFieldImpl);
	}

}
