package cz.mzk.recordmanager.server.model;

import cz.mzk.recordmanager.server.model.KramDnntLabel.DnntLabelEnum;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.MetadataUtils;
import cz.mzk.recordmanager.server.util.UrlValidatorUtils;

import static cz.mzk.recordmanager.server.imports.kramAvailability.KrameriusDocumentType.PAGE;
import static cz.mzk.recordmanager.server.imports.kramAvailability.KrameriusDocumentType.PERIODICAL_ITEM;

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

	public static EVersionUrl create(KramAvailability kramAvailability, boolean potentialDnnt) {
		EVersionUrl newUrl = new EVersionUrl();
		newUrl.setSource(kramAvailability.getHarvestedFrom().getIdPrefix());
		if (potentialDnnt && kramAvailability.getAvailability().equals("private")
				&& kramAvailability.isDnnt()
				&& kramAvailability.getDnntLabels().stream().anyMatch(l -> l.getLabel().equals(DnntLabelEnum.DNNTO.getLabel()))
				&& kramAvailability.getDnntLink() != null) {
			// dnnt online
			newUrl.setAvailability(Constants.DOCUMENT_AVAILABILITY_DNNT);
			newUrl.setLink(kramAvailability.getDnntLink());
		} else {
			newUrl.setAvailability(kramAvailability.getAvailability());
			newUrl.setLink(kramAvailability.getLink());
		}
		String comment = Constants.KRAM_EVERSION_COMMENT;
		if (kramAvailability.getType().equals(PAGE.getValue()) && kramAvailability.getPage() != null) {
			comment += " (s. " + kramAvailability.getPage() + ")";
		}
		if (kramAvailability.getType().equals(PERIODICAL_ITEM.getValue()) && kramAvailability.getIssue() != null) {
			comment += " (č. " + kramAvailability.getIssue() + ")";
		}
		newUrl.setComment(comment);
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

	public static EVersionUrl createDnnt(KramAvailability kramAvailability) {
		if (kramAvailability.getDnntLink() == null) return null;
		EVersionUrl url = create(kramAvailability.getHarvestedFrom().getIdPrefix(), Constants.DOCUMENT_AVAILABILITY_DNNT,
				kramAvailability.getDnntLink(), Constants.KRAM_EVERSION_COMMENT);
		return UrlValidatorUtils.doubleSlashUrlValidator().isValid(url.getLink()) ? url : null;
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
		return Constants.DOCUMENT_AVAILABILITY_UNKNOWN;
	}

	public void setAvailability(String availability) {
		if (availability.equals("public") || availability.equals(Constants.DOCUMENT_AVAILABILITY_ONLINE))
			this.availability = Constants.DOCUMENT_AVAILABILITY_ONLINE;
		else if (availability.equals("private") || availability.equals(Constants.DOCUMENT_AVAILABILITY_PROTECTED))
			this.availability = Constants.DOCUMENT_AVAILABILITY_PROTECTED;
		else if (availability.equals(Constants.DOCUMENT_AVAILABILITY_DNNT))
			this.availability = Constants.DOCUMENT_AVAILABILITY_DNNT;
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
		return 0;
	}

}
