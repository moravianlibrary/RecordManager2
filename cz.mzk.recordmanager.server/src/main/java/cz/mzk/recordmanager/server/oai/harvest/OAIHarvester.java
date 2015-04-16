package cz.mzk.recordmanager.server.oai.harvest;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import cz.mzk.recordmanager.server.oai.model.OAIGetRecord;
import cz.mzk.recordmanager.server.oai.model.OAIIdentify;
import cz.mzk.recordmanager.server.oai.model.OAIIdentifyRequest;
import cz.mzk.recordmanager.server.oai.model.OAIListIdentifiers;
import cz.mzk.recordmanager.server.oai.model.OAIRoot;
import cz.mzk.recordmanager.server.oai.model.OAIRecord;
import cz.mzk.recordmanager.server.util.HttpClient;
import cz.mzk.recordmanager.server.util.UrlUtils;

public class OAIHarvester {

	private static Logger logger = LoggerFactory.getLogger(OAIHarvester.class);
	
	private static final String OAI_VERB_LIST_RECORDS = "ListRecords";
	
	private static final String OAI_VERB_LIST_IDENTIFIERS = "ListIdentifiers";
	
	private static final String OAI_VERB_GET_RECORD = "GetRecord";
	
	private static final String OAI_VERB_IDENTIFY = "Identify";

	private final HttpClient httpClient;

	private final OAIHarvesterParams parameters;
	
	private final Unmarshaller unmarshaller;

	public OAIHarvester(HttpClient httpClient, OAIHarvesterParams parameters) {
		Preconditions.checkArgument(parameters.getUrl() != null, "missing url in parameters");
		Preconditions.checkArgument(parameters.getMetadataPrefix() != null, "missing metadataPrefix in parameters");
		this.parameters = parameters;
		this.httpClient = httpClient;
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(OAIRoot.class);
			this.unmarshaller = jaxbContext.createUnmarshaller();
		} catch (JAXBException je) {
			throw new RuntimeException(je);
		}
	}

	public OAIRoot listRecords(String resumptionToken) {
		String url = createUrl(resumptionToken, OAI_VERB_LIST_RECORDS, null);
		logger.info("About to harvest url: {}", url);
		try (InputStream is = httpClient.executeGet(url)) {
			OAIRoot oaiRoot = (OAIRoot) unmarshaller
					.unmarshal(is);
			logger.info("Finished harvesting of url: {}", url);
			return oaiRoot;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		} catch (JAXBException je) {
			throw new RuntimeException(je);
		}
	}
	
	public OAIListIdentifiers listIdentifiers(String resumptionToken) {
		String url = createUrl(resumptionToken, OAI_VERB_LIST_IDENTIFIERS, null);
		logger.info("About list identifiers from url: {}", url);
		try (InputStream is = httpClient.executeGet(url)) {
			OAIRoot oaiRoot = (OAIRoot) unmarshaller.unmarshal(is);
			logger.info("Finished listing identifiers from url: {}", url);
			return oaiRoot.getListIdentifiers();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		} catch (JAXBException je) {
			throw new RuntimeException(je);
		}
	}
	
	public OAIGetRecord getRecord(String identifier) {
		String url = createUrl(null, OAI_VERB_GET_RECORD, identifier);
		logger.info("About getRecord from url: {}", url);
		try (InputStream is = httpClient.executeGet(url)) {
			OAIRoot oaiRoot = (OAIRoot) unmarshaller
					.unmarshal(is);
			logger.info("Finished getting of record from url: {}", url);
			return oaiRoot.getGetRecord();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		} catch (JAXBException je) {
			throw new RuntimeException(je);
		}
	}
	
	public OAIIdentify identify() {
		String url = createIdentifyURL();
		logger.info("About send Identify request for url: {}", url);
		try (InputStream is = httpClient.executeGet(url)) {
			JAXBContext jaxbContext = JAXBContext.newInstance(OAIIdentifyRequest.class);
			Unmarshaller identifyUnmarshaller = jaxbContext.createUnmarshaller();
			OAIIdentifyRequest identifyReq = (OAIIdentifyRequest) identifyUnmarshaller
					.unmarshal(is);
			logger.info("Finished Identify request for url: {}", url);
			return identifyReq != null ? identifyReq.getIdentify() : null;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		} catch (JAXBException je) {
			throw new RuntimeException(je);
		}
	}

	private String createUrl(String resumptionToken, String verb, String recordIdentifier) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("verb", verb);
		if (resumptionToken == null) {
			params.put("metadataPrefix", parameters.getMetadataPrefix());
			if (parameters.getSet() != null && !verb.equals(OAI_VERB_GET_RECORD)) {
				params.put("set", parameters.getSet());
			}
			if (parameters.getFrom() != null) {
				params.put("from", parameters.getGranularity().dateToString(parameters.getFrom()));
			}
			if (parameters.getUntil() != null) {
				params.put("until", parameters.getGranularity().dateToString(parameters.getUntil()));
			}
		} else {
			params.put("resumptionToken", resumptionToken);
		}
		if (recordIdentifier != null) {
			params.put("identifier", recordIdentifier);
		}
		return UrlUtils.buildUrl(parameters.getUrl(), params);
	}
	
	private String createIdentifyURL() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("verb", OAI_VERB_IDENTIFY);
		return UrlUtils.buildUrl(parameters.getUrl(), params);
	}
}
