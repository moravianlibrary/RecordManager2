package cz.mzk.recordmanager.server.imports.kramAvailability;

import cz.mzk.recordmanager.server.kramerius.ApiMappingEnum;
import cz.mzk.recordmanager.server.kramerius.ApiMappingFactory;
import cz.mzk.recordmanager.server.kramerius.harvest.KrameriusHarvesterParams;
import cz.mzk.recordmanager.server.model.KramAvailability;
import cz.mzk.recordmanager.server.model.KrameriusConfiguration;
import cz.mzk.recordmanager.server.oai.dao.KrameriusConfigurationDAO;
import cz.mzk.recordmanager.server.scripting.Mapping;
import cz.mzk.recordmanager.server.util.FileUtils;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HttpClient;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import static cz.mzk.recordmanager.server.kramerius.ApiMappingEnum.AVAILABILITY_URL;

@Component
@StepScope
public class KramAvailabilityReader implements ItemReader<KramAvailability>, StepExecutionListener {

	@Autowired
	private KrameriusConfigurationDAO configDAO;

	@Autowired
	private HttpClient httpClient;

	@Autowired
	private HibernateSessionSynchronizer hibernateSync;

	@Autowired
	private ApiMappingFactory apiMappingFactory;

	private static final Logger logger = LoggerFactory.getLogger(KramAvailabilityReader.class);

	private KrameriusConfiguration config = null;

	private final Long configId;

	private KramAvailabilityXmlStreamReader reader;

	private final KrameriusHarvesterParams params = new KrameriusHarvesterParams();

	private ListIterator<String> iterator;
	private String url;

	private String source;
	private static final int ROWS = 100;
	private int start = 0;
	private final String filename;

	public KramAvailabilityReader(Long configId, String filename) {
		this.configId = configId;
		this.filename = filename;
		if (filename != null) {
			try {
				iterator = FileUtils.getFilesName(filename).listIterator();
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public KramAvailability read() {
		KramAvailability result;
		if (reader == null || !reader.hasNext()) {
			initializeReader();
		}
		while (reader.hasNext()) {
			try {
				result = reader.next();
				if (result != null) result.setHarvestedFrom(config);
				// !reader.hasNext() - end of file
				// result == null - empty file
				// iterator.hasNext() - exists next url
				if (!reader.hasNext() && result == null && iterator.hasNext()) {
					url = iterator.next();
					start = 0;
					initializeReader();
					continue;
				}
				return result;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	protected void initializeReader() throws RuntimeException {
		if (filename != null) {
			try {
				if (iterator.hasNext())
					reader = new KramAvailabilityXmlStreamReader(new FileInputStream(iterator.next()), params);
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
			return;
		}
		String localUrl = String.format(url, source, ROWS, start++ * ROWS);
		int error = 0;
		while (true) {
			try (InputStream is = httpClient.executeGet(localUrl)) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				IOUtils.copy(is, baos);
				byte[] bytes = baos.toByteArray();
				logger.info(localUrl);
				reader = new KramAvailabilityXmlStreamReader(new ByteArrayInputStream(bytes), params);
				error = 0;
				break;
			} catch (IOException e) {
				logger.error(e.getMessage());
				if (error++ >= 3) throw new RuntimeException();
			}
		}
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		try (HibernateSessionSynchronizer.SessionBinder sess = hibernateSync.register()) {
			if (config == null) {
				config = configDAO.get(configId);
			}
			if (config == null) {
				throw new IllegalArgumentException("Kramerius harvest configuration not found");
			}
			params.setApiMapping(apiMappingFactory.getMapping(processInfo(config.getAvailabilitySourceUrl())));
			if (filename == null) {
				iterator = params.getApiMappingValues(AVAILABILITY_URL).listIterator();
				source = config.getAvailabilitySourceUrl() + params.getApiMappingValue(ApiMappingEnum.API);
				if (iterator.hasNext()) url = iterator.next();
			}
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

	private static final List<String> API = Arrays.asList("5", "7");
	private static final String INFO_FORMAT = "%s%s/info";

	protected String processInfo(String sourceUrl) {
		for (String apiVersion : API) {
			Mapping mapping = apiMappingFactory.getMapping(apiVersion);
			try {
				String url = String.format(INFO_FORMAT, sourceUrl,
						mapping.getMapping().get(ApiMappingEnum.API.getValue()).get(0));
				logger.info("Harvesting info: " + url);
				try (InputStream is = httpClient.executeGet(url)) {
					return (new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8))).getString("version");
				} catch (IOException ioe) {
					logger.error(ioe.getMessage());
					throw new IOException("Info failed: " + url);
				}
			} catch (Exception e) {
				logger.info(e.getMessage());
			}
		}
		return null;
	}

}
