package cz.mzk.recordmanager.server.solr;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.mzk.recordmanager.server.index.SolrRecordProcessor;

public class SolrServerFacadeImpl implements SolrServerFacade {

	private static Logger logger = LoggerFactory.getLogger(SolrRecordProcessor.class);

	protected final SolrServer server;

	public SolrServerFacadeImpl(SolrServer server) {
		super();
		this.server = server;
	}

	@Override
	public void add(Collection<SolrInputDocument> documents, int commitWithinMs)
			throws IOException, SolrServerException {
		try {
			server.add(documents, commitWithinMs);
		} catch (SolrException | SolrServerException | IOException ex) {
			logger.error("Exception thrown during solr indexing, fallbacking to index one record at time", ex);
			fallbackIndex(documents, commitWithinMs);
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

	private void fallbackIndex(Collection<SolrInputDocument> documents, int commitWithinMs) {
		for (SolrInputDocument document : documents) {
			try {
				server.add(document, commitWithinMs);
			} catch (SolrException | SolrServerException | IOException ex) {
				logger.error(String.format("Exception thrown during indexing record: %s", document), ex);
			}
		}
	}

}
