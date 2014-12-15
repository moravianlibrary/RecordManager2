package cz.mzk.recordmanager.server.oai.harvest;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import cz.mzk.recordmanager.server.oai.model.OAIListRecords;

public class OAIHarvester {
	
	private static Logger logger = LoggerFactory.getLogger(OAIHarvester.class);

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
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			URI uri = createUrl(resumptionToken);
			logger.info("About to harvest url: {}", uri);
			CloseableHttpResponse result = httpClient.execute(new HttpGet(uri));
			InputStream is = result.getEntity().getContent();
			JAXBContext jc = JAXBContext.newInstance(OAIListRecords.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			OAIListRecords records = (OAIListRecords) unmarshaller.unmarshal(is);
			return records;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private URI createUrl(String resumptionToken) throws URISyntaxException {
		URIBuilder uriBuilder = new URIBuilder(url);
		if (resumptionToken == null) {
			uriBuilder.addParameter("metadataPrefix", metadataPrefix);
			if (set != null) {
				uriBuilder.addParameter("set", set);
			}
		} else {
			uriBuilder.addParameter("resumptionToken", resumptionToken);
		}
		uriBuilder.addParameter("verb", "ListRecords");
		return uriBuilder.build();
	}

}
