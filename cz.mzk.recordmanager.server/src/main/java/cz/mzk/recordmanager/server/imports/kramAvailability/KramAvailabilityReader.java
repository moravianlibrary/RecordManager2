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
import java.util.Arrays;
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

	private static final List<String> URLS = Arrays.asList(
			"%s/search?fl=dostupnost,dnnt,PID,level,dnnt-labels,document_type,issn&q=level:0&rows=%d&start=%d&wt=xml",
			"%s/search?fl=dostupnost,dnnt,PID,level,dnnt-labels,document_type&q=level:1+document_type:monographunit&rows=%d&start=%d&wt=xml"
	);

	private static final List<String> PAGE_URLS = Arrays.asList(
			"%s/search?fl=dostupnost,dnnt,PID,level,dnnt-labels,parent_pid,details,document_type&q=fedora.model:periodicalvolume&rows=%d&start=%d&wt=xml",
			"%s/search?fl=dostupnost,dnnt,PID,level,dnnt-labels,parent_pid,details,document_type,rok,issn&q=fedora.model:periodicalitem&rows=%d&start=%d&wt=xml",
			"%s/search?fl=dostupnost,dnnt,PID,level,dnnt-labels,parent_pid,details,document_type,title,rok,rels_ext_index&q=fedora.model:page+model_path:*periodicalitem/page&rows=%d&start=%d&wt=xml"
	);

	private final ListIterator<String> iterator;
	private String url;

	private String source;
	private static final int ROWS = 5000;
	private static int start = 0;
	private static final int BATCH_SIZE = 200;

	private static boolean done = false;

	public KramAvailabilityReader(Long configId, String type) {
		this.configId = configId;
		this.iterator = type.equals("titles") ? URLS.listIterator() : PAGE_URLS.listIterator();
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
