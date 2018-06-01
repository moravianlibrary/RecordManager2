package cz.mzk.recordmanager.server.kramerius.harvest;

import cz.mzk.recordmanager.server.kramerius.FedoraModels;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.solr.SolrServerFacade;
import cz.mzk.recordmanager.server.solr.SolrServerFactory;
import cz.mzk.recordmanager.server.solr.SolrServerFactoryImpl;
import cz.mzk.recordmanager.server.util.HttpClient;
import cz.mzk.recordmanager.server.util.SolrUtils;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractKrameriusHarvest implements IKrameriusHarvester {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractKrameriusHarvest.class);

	protected static final int MAX_TIME_ALLOWED = 100_000;

	protected Long harvestedFrom;

	protected KrameriusHarvesterParams params;

	protected HttpClient httpClient;

	protected SolrServerFactory solrServerFactory;

	private int start = 0;

	private String nextPid = "";

	protected AbstractKrameriusHarvest(HttpClient httpClient, SolrServerFactory solrServerFactory,
									   KrameriusHarvesterParams parameters, Long harvestedFrom) {
		this.harvestedFrom = harvestedFrom;
		this.params = parameters;
		this.httpClient = httpClient;
		this.solrServerFactory = solrServerFactory;
	}

	@Override
	public List<HarvestedRecord> getRecords(List<String> uuids) throws IOException {
		List<HarvestedRecord> records = new ArrayList<>();
		for (String uuid : uuids) {
			HarvestedRecord hr = downloadRecord(uuid);
			if (hr != null) {
				records.add(hr);
			} else {
				LOGGER.debug("Skipping HarvestedRecord with uuid: " + uuid + " [null value returned]");
			}
		}
		return records;
	}

	public HarvestedRecord downloadRecord(String uuid) throws IOException {
		HarvestedRecord.HarvestedRecordUniqueId id = new HarvestedRecord.HarvestedRecordUniqueId(harvestedFrom, uuid);
		HarvestedRecord unparsedHr = new HarvestedRecord(id);
		String url = createUrl(uuid);

		LOGGER.info("Harvesting record from: {}", url);
		try (InputStream is = httpClient.executeGet(url)) {
			if (is.markSupported()) {
				is.mark(Integer.MAX_VALUE);
				is.reset();
			}
			unparsedHr.setRawRecord(IOUtils.toByteArray(is));
		} catch (IOException ioe) {
			LOGGER.error(ioe.getMessage());
			throw new IOException("Harvesting record from: " + url + " caused IOException!");
		}
		return unparsedHr;
	}

	private String createUrl(String uuid) {
		final String baseUrl = params.getUrl();
		final String kramAPIItem = "/item/";
		final String kramAPIStream = "/streams/";
		final String kramStreamType = params.getMetadataStream();

		String resultingUrl = baseUrl + kramAPIItem + uuid + kramAPIStream + kramStreamType;
		LOGGER.trace("created URL: {}", resultingUrl);
		return resultingUrl;
	}

	protected SolrDocumentList executeSolrQuery(SolrQuery query) throws SolrServerException {
		SolrServerFacade solr = solrServerFactory.create(params.getUrl(), SolrServerFactoryImpl.Mode.KRAMERIUS);
		SolrDocumentList documents;
		try {
			QueryResponse response = solr.query(query);
			documents = response.getResults();
		} catch (SolrServerException sse) {
			LOGGER.error(sse.getMessage());
			throw new SolrServerException(String.format("Harvesting list of uuids from Kramerius API: caused " +
					"SolrServerException for model: %s, url:%s", params.getModel(), params.getUrl()));
		}
		return documents;
	}

	protected static List<String> getUuids(SolrDocumentList documents, String idField) throws SolrServerException {
		List<String> uuids = new ArrayList<>();
		for (SolrDocument document : documents) {
			for (Object pid : document.getFieldValues(idField)) {
				uuids.add(pid.toString());
			}
		}
		return uuids;
	}

	protected SolrQuery getBasicQuery(String... fields) {
		SolrQuery query = new SolrQuery();
		query.setQuery("*:*");

		//works with all possible models in single configuration
		String harvestedModelsStatement = String.join(" OR ", FedoraModels.HARVESTED_MODELS);
		query.add("fq", harvestedModelsStatement);

		if (params.getFrom() != null || params.getUntil() != null) {
			String range = SolrUtils.createDateRange(params.getFrom(), params.getUntil());
			query.add("fq", SolrUtils.createFieldQuery("modified_date", range));
		}

		query.setFields(fields);
		query.setRows((params.getQueryRows() != null) ? params.getQueryRows().intValue() : 10);
		query.setTimeAllowed(MAX_TIME_ALLOWED);
		return query;
	}

	@Override
	public Integer getStart() {
		return start;
	}

	@Override
	public void setStart(int start) {
		this.start = start;
	}

	@Override
	public String getNextPid() {
		return nextPid;
	}

	@Override
	public void setNextPid(String nextPid) {
		this.nextPid = nextPid;
	}
}
