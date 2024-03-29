package cz.mzk.recordmanager.server.kramerius.harvest;

import cz.mzk.recordmanager.server.util.DateUtils;
import cz.mzk.recordmanager.server.kramerius.ApiMappingEnum;
import cz.mzk.recordmanager.server.scripting.Mapping;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class KrameriusHarvesterParams {

	private String url;

	private String metadataStream;

	private String model;

	private Long queryRows;

	private Date from;

	private Date until;

	private String collection;

	private String authToken;

	private boolean downloadPrivateFulltexts;

	private String krameriusVersion;

	private Mapping apiMapping;

	public String getUrl() {
		return url;
	}

	public String getApiUrl() {
		return url + getApiMappingValue(ApiMappingEnum.API);
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMetadataStream() {
		return metadataStream;
	}

	public void setMetadataStream(String metadataStream) {
		this.metadataStream = metadataStream;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Date getFrom() {
		return from;
	}

	public void setFrom(Date from) throws ParseException {
		this.from = DateUtils.transform(from, "UTC");
	}

	public Date getUntil() {
		return until;
	}

	public void setUntil(Date until) {
		this.until = until;
	}

	public Long getQueryRows() {
		return this.queryRows;
	}

	public void setQueryRows(Long queryRows) {
		this.queryRows = queryRows;
	}

	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public Mapping getApiMapping() {
		return apiMapping;
	}

	public void setApiMapping(Mapping apiMapping) {
		this.apiMapping = apiMapping;
	}

	public String getApiMappingValue(String key) {
		List<String> results = apiMapping.getMapping().get(key);
		if (results == null || results.isEmpty()) return null;
		return results.get(0);
	}

	public String getApiMappingValue(ApiMappingEnum key) {
		return getApiMappingValue(key.getValue());
	}

	public List<String> getApiMappingValues(String key) {
		List<String> results = apiMapping.getMapping().get(key);
		if (results == null || results.isEmpty()) return null;
		return results;
	}

	public List<String> getApiMappingValues(ApiMappingEnum key) {
		return getApiMappingValues(key.getValue());
	}

	public String getKrameriusVersion() {
		return krameriusVersion;
	}

	public void setKrameriusVersion(String krameriusVersion) {
		this.krameriusVersion = krameriusVersion;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public boolean isDownloadPrivateFulltexts() {
		return downloadPrivateFulltexts;
	}

	public void setDownloadPrivateFulltexts(boolean downloadPrivateFulltexts) {
		this.downloadPrivateFulltexts = downloadPrivateFulltexts;
	}

	@Override
	public String toString() {
		return "KrameriusHarvesterParams{" +
				"url='" + url + '\'' +
				", metadataStream='" + metadataStream + '\'' +
				", model='" + model + '\'' +
				", queryRows=" + queryRows +
				", from=" + from +
				", until=" + until +
				", collection='" + collection + '\'' +
				'}';
	}
}
