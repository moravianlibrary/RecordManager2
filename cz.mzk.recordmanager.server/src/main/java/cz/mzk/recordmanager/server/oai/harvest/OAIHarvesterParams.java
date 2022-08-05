package cz.mzk.recordmanager.server.oai.harvest;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import cz.mzk.recordmanager.server.model.OAIGranularity;

public class OAIHarvesterParams {

	private String url;

	private String metadataPrefix;

	private String set;

	private OAIGranularity granularity;

	private Date from;

	private Date until;

	private String ictx;

	private String op;

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
	
	public OAIGranularity getGranularity() {
		return granularity;
	}

	public void setGranularity(OAIGranularity granularity) {
		this.granularity = granularity;
	}

	public Date getFrom() {
		return from;
	}

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	public void setFrom(Date from) throws ParseException {
		if (from != null) {
			from = DATE_FORMAT.parse(DATE_FORMAT.format(from));
			LocalDateTime ldt = LocalDateTime.ofInstant(from.toInstant(), ZoneId.systemDefault());
			ZonedDateTime utcZonedDateTime = ldt.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC"));
			from = DATE_FORMAT.parse(utcZonedDateTime.toInstant().toString());
		}
		this.from = from;
	}

	public Date getUntil() {
		return until;
	}

	public void setUntil(Date until) {
		this.until = until;
	}

	public String getIctx() {
		return ictx;
	}

	public void setIctx(String ictx) {
		this.ictx = ictx;
	}

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

	@Override
	public String toString() {
		return "OAIHarvesterParams [url=" + url + ", metadataPrefix="
				+ metadataPrefix + ", set=" + set + ", granularity="
				+ granularity + ", from=" + from + ", until=" + until + ']';
	}
	
	

}
