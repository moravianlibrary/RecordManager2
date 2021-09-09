package cz.mzk.recordmanager.server.imports.kramAvailability;

import cz.mzk.recordmanager.server.model.KramAvailability;
import cz.mzk.recordmanager.server.model.KrameriusConfiguration;
import cz.mzk.recordmanager.server.oai.dao.KrameriusConfigurationDAO;
import cz.mzk.recordmanager.server.util.HttpClient;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class KramAvailabilityReader implements ItemReader<List<KramAvailability>> {

	@Autowired
	private KrameriusConfigurationDAO configDAO;

	@Autowired
	private HttpClient httpClient;

	private static final Logger logger = LoggerFactory.getLogger(KramAvailabilityReader.class);

	private KrameriusConfiguration config = null;

	private final Long configId;

	private KramAvailabilityXmlStreamReader reader;

	private final ListIterator<String> iterator;
	private String url;

	private String source;
	private static final int ROWS = 5000;
	private int start = 0;
	private static final int BATCH_SIZE = 100;

	private boolean done = false;

	public KramAvailabilityReader(Long configId, KramAvailabilityHarvestType type) {
		this.configId = configId;
		this.iterator = UrlsListFactory.getUrls(type).listIterator();
		this.url = this.iterator.next();
	}

	@Override
	public synchronized List<KramAvailability> read() {
		if (done) return null;
		List<KramAvailability> results = new ArrayList<>();
		if (reader == null || !reader.hasNext()) {
			initializeReader();
		}
		while (reader.hasNext() && results.size() < BATCH_SIZE) {
			try {
				KramAvailability availability = reader.next();
				if (availability != null) {
					availability.setHarvestedFrom(config);
					results.add(availability);
				}
				// !reader.hasNext() - end of file
				// result == null - empty file
				if (!reader.hasNext() && availability == null) {
					// iterator.hasNext() - exists next url
					if (iterator.hasNext()) {
						url = iterator.next();
						start = 0;
					} else {
						done = true;
						break;
					}
					initializeReader();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return results.isEmpty() ? null : results;
	}

	protected synchronized void initializeReader() throws RuntimeException {
		if (config == null) {
			config = configDAO.get(configId);
			source = config.getAvailabilitySourceUrl();
		}
		String localUrl = String.format(url, source, ROWS, start++ * ROWS);
		int error = 0;
		while (true) {
			try (InputStream is = httpClient.executeGet(localUrl)) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				IOUtils.copy(is, baos);
				byte[] bytes = baos.toByteArray();
				logger.info(localUrl);
				reader = new KramAvailabilityXmlStreamReader(new ByteArrayInputStream(bytes));
				error = 0;
				break;
			} catch (IOException e) {
				logger.error(e.getMessage());
				if (error++ >= 3) throw new RuntimeException();
			}
		}
	}

}
