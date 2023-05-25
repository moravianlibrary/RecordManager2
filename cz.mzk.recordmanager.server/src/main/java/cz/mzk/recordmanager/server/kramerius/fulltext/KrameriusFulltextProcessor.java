package cz.mzk.recordmanager.server.kramerius.fulltext;

import cz.mzk.recordmanager.server.dc.InvalidDcException;
import cz.mzk.recordmanager.server.kramerius.ApiMappingEnum;
import cz.mzk.recordmanager.server.kramerius.ApiMappingFactory;
import cz.mzk.recordmanager.server.kramerius.harvest.KrameriusHarvesterParams;
import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.model.FulltextKramerius;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.KrameriusConfiguration;
import cz.mzk.recordmanager.server.oai.dao.FulltextKrameriusDAO;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.oai.dao.KrameriusConfigurationDAO;
import cz.mzk.recordmanager.server.scripting.Mapping;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;
import cz.mzk.recordmanager.server.util.HttpClient;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.marc4j.marc.InvalidMARCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KrameriusFulltextProcessor implements
		ItemProcessor<HarvestedRecord, HarvestedRecord>, StepExecutionListener {

	private static final Logger logger = LoggerFactory
			.getLogger(KrameriusFulltextProcessor.class);

	@Autowired
	private KrameriusFulltexterFactory krameriusFulltexterFactory;

	private KrameriusFulltexter fulltexter;

	@Autowired
	private KrameriusConfigurationDAO configDao;

	@Autowired
	private HarvestedRecordDAO recordDao;

	@Autowired
	private FulltextKrameriusDAO fmDao;

	@Autowired
	private HibernateSessionSynchronizer sync;

	@Autowired
	private MetadataRecordFactory metadataRecordFactory;

	@Autowired
	private ApiMappingFactory apiMappingFactory;

	@Autowired
	private HttpClient httpClient;

	// configuration
	private final Long confId;

	private boolean downloadPrivateFulltexts;

	public KrameriusFulltextProcessor(Long confId) {
		super();
		this.confId = confId;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		try (SessionBinder sess = sync.register()) {
			KrameriusConfiguration config = configDao.get(confId);
			if (config == null) {
				throw new IllegalArgumentException(String.format(
						"Kramerius configuration with id=%s not found", confId));
			}
			KrameriusHarvesterParams params = new KrameriusHarvesterParams();
			params.setUrl(config.getUrl());
			params.setMetadataStream(config.getMetadataStream());
			params.setQueryRows(config.getQueryRows());
			params.setCollection(config.getCollection());
			params.setAuthToken(config.getAuthToken());
			params.setDownloadPrivateFulltexts(config.isDownloadPrivateFulltexts());
			downloadPrivateFulltexts = config.isDownloadPrivateFulltexts();
			processInfo(params);
			params.setApiMapping(apiMappingFactory.getMapping(params.getKrameriusVersion()));
			fulltexter = krameriusFulltexterFactory.create(config, params);
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

	@Override
	public HarvestedRecord process(HarvestedRecord item) throws Exception {
		logger.debug("Processing Harvested Record: " + item
				+ " uniqueId: " + item.getUniqueId());

		String policy;
		String model;
		
		// read complete HarvestedRecord using DAO
		HarvestedRecord rec = recordDao.findByIdAndHarvestConfiguration(item
				.getUniqueId().getRecordId(), confId);
		MetadataRecord mr;
		// get Kramerius policy from record
		try {
			mr = metadataRecordFactory.getMetadataRecord(rec);
			policy = mr.getPolicyKramerius();
			model = mr.getModelKramerius();
		} catch ( InvalidDcException e) {
			logger.warn("InvalidDcException for record with id:" + item.getUniqueId());
			logger.warn(e.getMessage());
			//doesn't do anything, just returns rec from DAO and writes a message into log
			return rec;
		} catch (InvalidMARCException e) {
			logger.warn("InvalidMarcException for record with id:" + item.getUniqueId());
			logger.warn(e.getMessage());
			//doesn't do anything, just returns rec from DAO and writes a message into log
			return rec;
		} catch (Exception e) {
			logger.warn(e.getMessage());
			return rec;
		}

		// modify read HarvestedRecord only if following condition is fulfilled
		if (policy.equals("public") || downloadPrivateFulltexts) {
			logger.debug("Processor: privacy condition fulfilled, reading pages");

			String rootUuid = rec.getUniqueId().getRecordId();

			List<String> fulltexterMethod;
			if (model.equals("periodical") || (model.equals("unknown") && !mr.getISSNs().isEmpty())) {
				fulltexterMethod = Arrays.asList("periodical", "monograph");
			} else fulltexterMethod = Arrays.asList("monograph", "periodical");

			List<FulltextKramerius> pages = new ArrayList<>();
			for (String method : fulltexterMethod) {
				switch (method) {
				case "periodical":
					logger.info("Using (periodical) fultexter \"for root\" for uuid " + rootUuid + '.');
					pages = fulltexter.getFulltextForRoot(rootUuid);
					break;
				default:
					logger.info("Using (monograph/default) fultexter \"for parent\" for uuid " + rootUuid + '.');
					pages = fulltexter.getFulltextObjects(rootUuid);
					break;
				}
				if (!pages.isEmpty()) break;
			}

			// if we got empty list in pages => do nothing, return original record
			if (pages.isEmpty()) {
				return rec;
			}
			
			//delete old FulltextKramerius from database before adding new ones
			fmDao.deleteFulltext(rec.getId());

			rec.setFulltextKramerius(pages);
		} else {
			logger.debug("Processor: privacy condition is NOT fulfilled, skipping record");
		}

		return rec;
	}

	private static final String INFO_FORMAT = "%s%s/info";

	protected void processInfo(KrameriusHarvesterParams params) {
		for (String apiVersion : ApiMappingFactory.API_VERSION) {
			Mapping mapping = apiMappingFactory.getMapping(apiVersion);
			try {
				JSONObject info = info(String.format(INFO_FORMAT, params.getUrl(),
						mapping.getMapping().get(ApiMappingEnum.API.getValue()).get(0)));
				params.setKrameriusVersion(info.getString("version"));
				return;
			} catch (Exception e) {
				logger.info(e.getMessage());
			}
		}
	}

	public JSONObject info(String url) throws IOException {
		logger.info("Harvesting info: " + url);
		try (InputStream is = httpClient.executeGet(url)) {
			return new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8));
		} catch (IOException ioe) {
			logger.error(ioe.getMessage());
			throw new IOException("Info failed: " + url);
		}
	}

}
