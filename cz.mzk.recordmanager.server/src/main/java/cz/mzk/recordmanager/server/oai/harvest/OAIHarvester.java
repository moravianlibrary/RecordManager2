package cz.mzk.recordmanager.server.oai.harvest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import cz.mzk.recordmanager.server.oai.model.OAIListRecords;
import cz.mzk.recordmanager.server.util.ApacheHttpClient;
import cz.mzk.recordmanager.server.util.HttpClient;
import cz.mzk.recordmanager.server.util.UrlUtils;

public class OAIHarvester {

	private static Logger logger = LoggerFactory.getLogger(OAIHarvester.class);

	private HttpClient httpClient = new ApacheHttpClient();

	private final String url;

	private final String metadataPrefix;

	private final String set;

	private final Date from;

	private final Date until;

	public OAIHarvester(String url, String metadataPrefix, String set) {
		this(url, metadataPrefix, set, null, null);
	}

	public OAIHarvester(String url, String metadataPrefix, String set,
			Date from, Date until) {
		super();
		Preconditions.checkNotNull(url, "url");
		Preconditions.checkNotNull(metadataPrefix, "metadataPrefix");
		this.url = url;
		this.metadataPrefix = metadataPrefix;
		this.set = set;
		this.from = from;
		this.until = until;

	}

	public OAIListRecords listRecords(String resumptionToken) {
		String url = createUrl(resumptionToken);
		logger.info("About to harvest url: {}", url);
		try (InputStream is = httpClient.executeGet(url)) {
			JAXBContext jc = JAXBContext.newInstance(OAIListRecords.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			OAIListRecords records = (OAIListRecords) unmarshaller
					.unmarshal(is);
			return records;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		} catch (JAXBException je) {
			throw new RuntimeException(je);
		}
	}

	private String createUrl(String resumptionToken) {
		Map<String, String> params = new HashMap<String, String>();
		if (resumptionToken == null) {
			params.put("metadataPrefix", metadataPrefix);
			if (set != null) {
				params.put("set", set);
			}
		} else {
			params.put("resumptionToken", resumptionToken);
		}
		params.put("verb", "ListRecords");
		return UrlUtils.buildUrl(url, params);
	}

}
