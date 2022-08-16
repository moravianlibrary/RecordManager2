package cz.mzk.recordmanager.server.model;

import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.MetadataUtils;
import cz.mzk.recordmanager.server.util.UrlValidatorUtils;
import cz.mzk.recordmanager.server.util.constants.EVersionConstants;

public class EVersionUrl implements Comparable {

	private static final String SPLITTER = "\\|";

	private String source;
	private String availability;
	private String link;
	private String comment;

	public static EVersionUrl create(String rawData) throws Exception {
		String[] splitData = rawData.split(SPLITTER);
		if (splitData.length < 3) throw new Exception("Bad url data");
		return create(splitData[0], splitData[1], splitData[2], splitData.length == 4 ? splitData[3] : "");
	}

	public static EVersionUrl create(KramAvailability kramAvailability) {
		return create(kramAvailability, EVersionConstants.DIGITIZED_LINK);
	}

	public static EVersionUrl create(KramAvailability kramAvailability, String comment) {
		return create(kramAvailability.getHarvestedFrom().getIdPrefix(),
				kramAvailability.getAvailability(), kramAvailability.getLink(), comment);
	}

	public static EVersionUrl create(String source, String availability, String link, String comment) {
		if (!UrlValidatorUtils.doubleSlashUrlValidator().isValid(link)) return null;
		EVersionUrl newUrl = new EVersionUrl();
		newUrl.setSource(source);
		newUrl.setAvailability(availability);
		newUrl.setLink(link);
		newUrl.setComment(comment);
		return newUrl;
	}

	public static EVersionUrl createDnnt(KramAvailability kramAvailability) {
		return createDnnt(kramAvailability, EVersionConstants.DIGITIZED_LINK);
	}

	public static EVersionUrl createDnnt(KramAvailability kramAvailability, String comment) {
		if (kramAvailability.getDnntLink() == null) return null;
		return create(kramAvailability.getHarvestedFrom().getIdPrefix(), Constants.DOCUMENT_AVAILABILITY_DNNT,
				kramAvailability.getDnntLink(), comment);
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
		if (availability.equals(Constants.DOCUMENT_AVAILABILITY_DNNT))
			return Constants.DOCUMENT_AVAILABILITY_DNNT;
		if (availability.equals(Constants.DOCUMENT_AVAILABILITY_NA))
			return Constants.DOCUMENT_AVAILABILITY_NA;
		if (availability.equals(Constants.DOCUMENT_AVAILABILITY_MEMBER))
			return Constants.DOCUMENT_AVAILABILITY_MEMBER;
		return Constants.DOCUMENT_AVAILABILITY_UNKNOWN;
	}

	public void setAvailability(String availability) {
		if (availability.equals("public") || availability.equals(Constants.DOCUMENT_AVAILABILITY_ONLINE))
			this.availability = Constants.DOCUMENT_AVAILABILITY_ONLINE;
		else if (availability.equals("private") || availability.equals(Constants.DOCUMENT_AVAILABILITY_PROTECTED))
			this.availability = Constants.DOCUMENT_AVAILABILITY_PROTECTED;
		else if (availability.equals(Constants.DOCUMENT_AVAILABILITY_DNNT))
			this.availability = Constants.DOCUMENT_AVAILABILITY_DNNT;
		else if (availability.equals(Constants.DOCUMENT_AVAILABILITY_NA))
			this.availability = Constants.DOCUMENT_AVAILABILITY_NA;
		else if (availability.equals(Constants.DOCUMENT_AVAILABILITY_MEMBER))
			this.availability = Constants.DOCUMENT_AVAILABILITY_MEMBER;
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
		if (this.availability.equals(Constants.DOCUMENT_AVAILABILITY_DNNT)) return 1;
		if (other.getAvailability().equals(Constants.DOCUMENT_AVAILABILITY_DNNT)) return -1;
		if (this.availability.equals(Constants.DOCUMENT_AVAILABILITY_PROTECTED)) return 1;
		if (other.getAvailability().equals(Constants.DOCUMENT_AVAILABILITY_PROTECTED)) return -1;
		if (this.availability.equals(Constants.DOCUMENT_AVAILABILITY_MEMBER)) return 1;
		if (other.getAvailability().equals(Constants.DOCUMENT_AVAILABILITY_MEMBER)) return -1;
		if (this.availability.equals(Constants.DOCUMENT_AVAILABILITY_NA)) return 1;
		if (other.getAvailability().equals(Constants.DOCUMENT_AVAILABILITY_NA)) return -1;
		return 0;
	}

}
