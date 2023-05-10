package cz.mzk.recordmanager.server.kramerius.harvest;

import cz.mzk.recordmanager.server.kramerius.ApiMappingEnum;
import cz.mzk.recordmanager.server.kramerius.ApiMappingFactory;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.KrameriusConfiguration;
import cz.mzk.recordmanager.server.oai.dao.KrameriusConfigurationDAO;
import cz.mzk.recordmanager.server.scripting.Mapping;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;
import org.apache.solr.client.solrj.SolrServerException;
import org.json.JSONObject;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@StepScope
public class KrameriusItemReader implements ItemReader<List<HarvestedRecord>>,
		StepExecutionListener {

	@Autowired
	private KrameriusConfigurationDAO configDao;

	@Autowired
	private KrameriusHarvesterFactory harvesterFactory;

	@Autowired
	private HibernateSessionSynchronizer hibernateSync;

	@Autowired
	private ApiMappingFactory apiMappingFactory;

	private KrameriusHarvester kHarvester;

	// configuration
	private final Long confId;

	private final Date fromDate;
	private final Date untilDate;
	private final KrameriusHarvesterEnum type;
	private final String inFile;

	public KrameriusItemReader(Long confId, Date fromDate, Date untilDate, String type, String inFile) {
		super();
		this.confId = confId;
		this.fromDate = fromDate;
		this.untilDate = untilDate;
		if (inFile != null) this.type = KrameriusHarvesterEnum.FILE;
		else if (type == null) this.type = KrameriusHarvesterEnum.EMPTY;
		else this.type = KrameriusHarvesterEnum.stringToHarvesterEnum(type.toLowerCase());
		this.inFile = inFile;
	}

	@Override
	public List<HarvestedRecord> read() throws SolrServerException, IOException {
		// get uuids
		List<String> uuids = kHarvester.getNextUuids();
		if (uuids == null) return null;
		// return metadata
		return kHarvester.getRecords(uuids);
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		try (SessionBinder sess = hibernateSync.register()) {
			KrameriusConfiguration conf = configDao.get(confId);
			if (conf == null) {
				throw new IllegalArgumentException(String.format(
						"Kramerius harvest configuration with id=%s not found", confId));
			}
			KrameriusHarvesterParams params = new KrameriusHarvesterParams();
			params.setUrl(conf.getUrl());
			params.setMetadataStream(conf.getMetadataStream());
			params.setQueryRows(conf.getQueryRows());
			params.setAuthToken(conf.getAuthToken());
			params.setFrom(fromDate);
			params.setUntil(untilDate);
			params.setCollection(conf.getCollection());
			params.setDownloadPrivateFulltexts(conf.isDownloadPrivateFulltexts());
			kHarvester = harvesterFactory.create(type, params, confId, inFile);
			processInfo(params);
			params.setApiMapping(apiMappingFactory.getMapping(params.getKrameriusVersion()));
		} catch (ParseException e) {
			throw new RuntimeException("Cannot parse 'from' parameter", e);
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

	private static final String INFO_FORMAT = "%s%s/info";

	protected void processInfo(KrameriusHarvesterParams params) {
		for (String apiVersion : ApiMappingFactory.API_VERSION) {
			Mapping mapping = apiMappingFactory.getMapping(apiVersion);
			try {
				JSONObject info = kHarvester.info(String.format(INFO_FORMAT, params.getUrl(),
						mapping.getMapping().get(ApiMappingEnum.API.getValue()).get(0)));
				params.setKrameriusVersion(info.getString("version"));
				return;
			} catch (Exception e) {
				KrameriusHarvesterImpl.LOGGER.info(e.getMessage());
			}
		}
	}

}
