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

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class KramAvailabilityReader implements ItemReader<KramAvailability> {

	@Autowired
	private KrameriusConfigurationDAO configDAO;

	@Autowired
	private HttpClient httpClient;

	private static final Logger logger = LoggerFactory.getLogger(KramAvailabilityReader.class);

	private KrameriusConfiguration config = null;

	private final Long configId;

	private KramAvailabilityXmlStreamReader reader;

	private final List<String> URLS = Arrays.asList(
			"%s/search?fl=dostupnost,dnnt,PID,level,dnnt-labels&q=level:0&rows=%d&start=%d&wt=xml",
			"%s/search?fl=dostupnost,dnnt,PID,level,dnnt-labels&q=level:1+document_type:monographunit&rows=%d&start=%d&wt=xml"
	);

	private final ListIterator<String> iterator;
	private String url;

	private String source;
	private static final int ROWS = 100;
	private int start = 0;
	private final String filename;

	public KramAvailabilityReader(Long configId, String filename) {
		this.configId = configId;
		this.filename = filename;
		if (filename != null) iterator = Arrays.asList(filename.split(",")).listIterator();
		else this.iterator = URLS.listIterator();
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
		if (config == null) {
			config = configDAO.get(configId);
			if (filename == null) {
				source = config.getAvailabilitySourceUrl();
				if (iterator.hasNext()) url = iterator.next();
			}
		}
		if (filename != null) {
			try {
				if (iterator.hasNext())
					reader = new KramAvailabilityXmlStreamReader(new FileInputStream(iterator.next()));
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
