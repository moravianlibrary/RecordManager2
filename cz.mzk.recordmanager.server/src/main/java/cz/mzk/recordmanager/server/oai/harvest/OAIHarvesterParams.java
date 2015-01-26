package cz.mzk.recordmanager.server.oai.harvest;

import java.util.Date;

public class OAIHarvesterParams {

	private String url;

	private String metadataPrefix;

	private String set;
	
	private String granularity;

	private Date from;

	private Date until;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMetadataPrefix() {
		return metadataPrefix;
	}

	public void setMetadataPrefix(String metadataPrefix) {
		this.metadataPrefix = metadataPrefix;
	}

	public String getSet() {
		return set;
	}

	public void setSet(String set) {
		this.set = set;
	}
	
	public String getGranularity() {
		return granularity;
	}

	public void setGranularity(String granularity) {
		this.granularity = granularity;
	}

	public Date getFrom() {
		return from;
	}

	public void setFrom(Date from) {
		this.from = from;
	}

	public Date getUntil() {
		return until;
	}

	public void setUntil(Date until) {
		this.until = until;
	}

	@Override
	public String toString() {
		return "OAIHarvesterParams [url=" + url + ", metadataPrefix="
				+ metadataPrefix + ", set=" + set + ", granularity="
				+ granularity + ", from=" + from + ", until=" + until + "]";
	}
	
	

}
