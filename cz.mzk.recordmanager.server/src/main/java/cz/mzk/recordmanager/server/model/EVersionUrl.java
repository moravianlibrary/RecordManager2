package cz.mzk.recordmanager.server.model;

import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.MetadataUtils;

public class EVersionUrl implements Comparable {
	private static final String SPLITTER = "\\|";

	private String source;
	private String availability;
	private String link;
	private String comment;

	public static EVersionUrl create(String rawData) throws Exception {
		String[] splitData = rawData.split(SPLITTER);
		if (splitData.length < 3) throw new Exception("Bad url data");
		EVersionUrl newUrl = new EVersionUrl();
		newUrl.setSource(splitData[0]);
		newUrl.setAvailability(splitData[1]);
		newUrl.setLink(splitData[2]);
		newUrl.setComment(splitData.length == 4 ? splitData[3] : "");
		return newUrl;
	}

	public static EVersionUrl create(KramAvailability kramAvailability) {
		EVersionUrl newUrl = new EVersionUrl();
		newUrl.setSource(kramAvailability.getHarvestedFrom().getIdPrefix());
		newUrl.setAvailability(kramAvailability.getAvailability());
		newUrl.setLink(kramAvailability.getLink());
		newUrl.setComment(Constants.KRAM_EVERSION_COMMENT);
		return newUrl;
	}

	public static EVersionUrl create(String source, String availability, String link, String comment) {
		EVersionUrl newUrl = new EVersionUrl();
		newUrl.setSource(source);
		newUrl.setAvailability(availability);
		newUrl.setLink(link);
		newUrl.setComment(comment);
		return newUrl;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getAvailability() {
		if (availability.equals("public") || availability.equals(Constants.DOCUMENT_AVAILABILITY_ONLINE))
			return Constants.DOCUMENT_AVAILABILITY_ONLINE;
		if (availability.equals("private") || availability.equals(Constants.DOCUMENT_AVAILABILITY_PROTECTED))
			return Constants.DOCUMENT_AVAILABILITY_PROTECTED;
		return Constants.DOCUMENT_AVAILABILITY_UNKNOWN;
	}

	public void setAvailability(String availability) {
		if (availability.equals("public") || availability.equals(Constants.DOCUMENT_AVAILABILITY_ONLINE))
			this.availability = Constants.DOCUMENT_AVAILABILITY_ONLINE;
		else if (availability.equals("private") || availability.equals(Constants.DOCUMENT_AVAILABILITY_PROTECTED))
			this.availability = Constants.DOCUMENT_AVAILABILITY_PROTECTED;
		else this.availability = Constants.DOCUMENT_AVAILABILITY_UNKNOWN;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	@Override
	public String toString() {
		return MetadataUtils.generateUrl(this.source, this.availability, this.link, this.comment);
	}

	@Override
	public int compareTo(Object o) {
		if (this == o || o == null || this.getClass() != o.getClass()) return 0;
		EVersionUrl other = (EVersionUrl) o;
		if (this.toString().equals(o.toString())) return 0;
		if (this.availability.equals(other.getAvailability())) return 1;
		if (this.availability.equals(Constants.DOCUMENT_AVAILABILITY_ONLINE)) return 1;
		if (other.getAvailability().equals(Constants.DOCUMENT_AVAILABILITY_ONLINE)) return -1;
		if (this.availability.equals(Constants.DOCUMENT_AVAILABILITY_PROTECTED)) return 1;
		if (other.getAvailability().equals(Constants.DOCUMENT_AVAILABILITY_PROTECTED)) return -1;
		return 0;
	}

}
