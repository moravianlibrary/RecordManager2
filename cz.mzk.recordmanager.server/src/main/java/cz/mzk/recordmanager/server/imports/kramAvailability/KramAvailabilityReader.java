package cz.mzk.recordmanager.server.imports.kramAvailability;

import cz.mzk.recordmanager.server.model.KramAvailability;
import cz.mzk.recordmanager.server.model.KrameriusConfiguration;
import cz.mzk.recordmanager.server.oai.dao.KrameriusConfigurationDAO;
import cz.mzk.recordmanager.server.util.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class KramAvailabilityReader implements ItemReader<KramAvailability> {

	@Autowired
	private KrameriusConfigurationDAO configDAO;

	@Autowired
	private HttpClient httpClient;

	private static Logger logger = LoggerFactory.getLogger(KramAvailabilityReader.class);

	private KrameriusConfiguration config = null;

	private Long configId;

	private KramAvailabilityXmlStreamReader reader;

	private static final String url = "%s/search?fl=dostupnost,dnnt,PID&q=level:0&rows=%d&start=%d";

	private String source;
	private static final int ROWS = 100;
	private int start = 0;

	public KramAvailabilityReader(Long configId) {
		this.configId = configId;
	}

	@Override
	public KramAvailability read() {
		KramAvailability result = null;
		if (reader == null || !reader.hasNext()) {
			initializeReader();
		}
		if (reader.hasNext()) {
			try {
				result = reader.next();
				if (result != null) result.setHarvestedFrom(config);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	protected void initializeReader() throws RuntimeException {
		if (config == null) {
			config = configDAO.get(configId);
			source = config.getAvailabilitySourceUrl();
		}
		String localUrl = String.format(url, source, ROWS, start++ * ROWS);
		int error = 0;
		while (true) {
			try {
				logger.info(localUrl);
				reader = new KramAvailabilityXmlStreamReader(httpClient.executeGet(localUrl));
				error = 0;
				break;
			} catch (IOException e) {
				logger.error(e.getMessage());
				if (error++ >= 3) throw new RuntimeException();
			}
		}
	}

}
