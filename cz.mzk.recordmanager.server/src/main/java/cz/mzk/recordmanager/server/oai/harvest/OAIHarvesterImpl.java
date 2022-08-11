package cz.mzk.recordmanager.server.oai.harvest;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;

import cz.mzk.recordmanager.server.util.CleaningUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import cz.mzk.recordmanager.server.oai.model.OAIGetRecord;
import cz.mzk.recordmanager.server.oai.model.OAIIdentify;
import cz.mzk.recordmanager.server.oai.model.OAIListIdentifiers;
import cz.mzk.recordmanager.server.oai.model.OAIListRecords;
import cz.mzk.recordmanager.server.oai.model.OAIRoot;
import cz.mzk.recordmanager.server.util.HttpClient;
import cz.mzk.recordmanager.server.util.UrlUtils;

public class OAIHarvesterImpl implements OAIHarvester {

	private static Logger logger = LoggerFactory.getLogger(OAIHarvesterImpl.class);

	private final Set<String> IGNORED_ERROR_CODES = ImmutableSet.of( //
			"noRecordsMatch" // Thrown by OAI providers on empty result set
	);

	private static final String OAI_VERB_LIST_RECORDS = "ListRecords";
	
	private static final String OAI_VERB_LIST_IDENTIFIERS = "ListIdentifiers";
	
	private static final String OAI_VERB_GET_RECORD = "GetRecord";
	
	private static final String OAI_VERB_IDENTIFY = "Identify";

	private final HttpClient httpClient;

	private final OAIHarvesterParams parameters;
	
	private final Unmarshaller unmarshaller;

	public OAIHarvesterImpl(HttpClient httpClient, OAIHarvesterParams parameters) {
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

	/**
	 * send ListRecords request
	 * @param resumptionToken {@link String} value from the previous request
	 * @return {@link OAIListRecords}
	 */
	@Override
	public OAIListRecords listRecords(String resumptionToken) {
		String url = createUrl(resumptionToken, OAI_VERB_LIST_RECORDS, null);
		OAIRoot rootElement = sendOaiRequest(url, "About to harvest url: {}", "Finished harvesting of url: {}");
		return rootElement.getListRecords();
	}
	
	/**
	 * send ListIdentifiers request
	 * @param resumptionToken {@link String} value from the previous request
	 * @return {@link OAIListIdentifiers}
	 */
	@Override
	public OAIListIdentifiers listIdentifiers(String resumptionToken) {
		String url = createUrl(resumptionToken, OAI_VERB_LIST_IDENTIFIERS, null);
		OAIRoot rootElement = sendOaiRequest(url, "", "Finished listing identifiers from url: {}");
		return rootElement.getListIdentifiers();
	}
	
	/**
	 * send GetRecordRequest
	 * @param identifier OAI identifier of record
	 * @return {@link OAIGetRecord}
	 */
	@Override
	public OAIGetRecord getRecord(String identifier) {
		String url = createUrl(null, OAI_VERB_GET_RECORD, identifier);
		OAIRoot rootElement = sendOaiRequest(url, "",  "Finished getting of record from url: {}");
		return rootElement.getGetRecord();
	}
	
	/**
	 * send Identify request
	 * @return {@link OAIIdentify}
	 */
	@Override
	public OAIIdentify identify() {
		String url = createIdentifyURL();
		OAIRoot rootElement = sendOaiRequest(url, "About send Identify request for url: {}", "Finished Identify request for url: {}");
		return rootElement.getIdentify();
	}
	
	
	/**
	 * Send OAI request and unmarshall response.
	 * If request failes, content of document is logged and 
	 * request is performed once again
	 * @param url URL of next resumptionToken
	 * @param preLogMessage message used for logging purposes, URL is injected via {} notation
	 * @param postLogMessage message used for logging purposes, URL is injected via {} notation
	 * @return {@link OAIRoot}
	 */
	protected OAIRoot sendOaiRequest(String url, String preLogMessage, String postLogMessage) {
		OAIRoot rootElement;
		try {
			rootElement = sendOaiRequestThrowing(url, preLogMessage, postLogMessage);
			return rootElement;
		} catch (OaiErrorException e) {
			logger.warn(e.getMessage());
		}
		logger.info("OAI request failed, trying once again");
		try {
			rootElement = sendOaiRequestThrowing(url, preLogMessage, postLogMessage);
			return rootElement;
		} catch (OaiErrorException oex) {
			throw new RuntimeException(oex);
		}
	}

	/**
	 * Send OAI request and unmarshall response 
	 * @param url URL of next resumptionToken
	 * @param preLogMessage message used for logging purposes, URL is injected via {} notation
	 * @param postLogMessage message used for logging purposes, URL is injected via {} notation
	 * @return {@link OAIRoot}
	 * @throws OaiErrorException OAI error
	 */
	protected OAIRoot sendOaiRequestThrowing(String url, String preLogMessage, String postLogMessage) throws OaiErrorException {
		if (!preLogMessage.isEmpty()) {
			logger.info(preLogMessage, url);
		}
		String rawMessage = "";
		try (InputStream isResult = httpClient.executeGet(url)){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOUtils.copy(isResult, baos);
			byte[] bytes = baos.toByteArray();
			InputStream is = new ByteArrayInputStream(bytes);
			if (is.markSupported()) {
				is.mark(Integer.MAX_VALUE);
				rawMessage = IOUtils.toString(is);
				is.reset();
			}
			OAIRoot oaiRoot;
			try {
				oaiRoot = (OAIRoot) unmarshaller.unmarshal(is);
			} catch (UnmarshalException ex) {
				logger.warn("Invalid XML characters");
				oaiRoot = (OAIRoot) unmarshaller.unmarshal(
						CleaningUtils.removeInvalidXMLCharacters(new ByteArrayInputStream(bytes)));
			}
			if (!postLogMessage.isEmpty()) {
				logger.info(postLogMessage, url);
			}
			if (oaiRoot.getOaiError() != null && !IGNORED_ERROR_CODES.contains(oaiRoot.getOaiError().getCode())) {
				throw new OaiErrorException(oaiRoot.getOaiError().getMessage());
			}
			return oaiRoot;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		} catch (JAXBException je) {
			StringBuilder exceptionStr = new StringBuilder();
			exceptionStr.append("Problem with harvested document");
			if (!rawMessage.isEmpty()) {
				exceptionStr.append(rawMessage);
			}
			OaiErrorException oex = new OaiErrorException(exceptionStr.toString(), je);
			throw oex;
		}
	}
	
	/**
	 * Create URL for OAI request from given arguments and stored parmeters
	 * @param resumptionToken {@link String} value from the previous request
	 * @param verb {@link String } type of harvest
	 * @param recordIdentifier {@link String} Id of single record
	 * @return {@link String} URL
	 */
	protected String createUrl(String resumptionToken, String verb, String recordIdentifier) {
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
		if (parameters.getIctx() != null) {
			params.put("ictx", parameters.getIctx());
		}
		if (parameters.getOp() != null) {
			params.put("op", parameters.getOp());
		}
		if (recordIdentifier != null) {
			params.put("identifier", recordIdentifier);
		}
		return UrlUtils.buildUrl(parameters.getUrl(), params);
	}
	
	/**
	 * create URL for Identify request
	 * @return url
	 */
	protected String createIdentifyURL() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("verb", OAI_VERB_IDENTIFY);
		if (parameters.getIctx() != null) {
			params.put("ictx", parameters.getIctx());
		}
		if (parameters.getOp() != null) {
			params.put("op", parameters.getOp());
		}
		return UrlUtils.buildUrl(parameters.getUrl(), params);
	}
}
