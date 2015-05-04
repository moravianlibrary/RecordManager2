package cz.mzk.recordmanager.server.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.validator.routines.ISBNValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat;
import cz.mzk.recordmanager.server.model.Isbn;

public class MetadataDublinCoreRecord implements MetadataRecord {

	private static Logger logger = LoggerFactory
			.getLogger(MetadataDublinCoreRecord.class);

	protected DublinCoreRecord dcRecord;

	protected final ISBNValidator isbnValidator = ISBNValidator
			.getInstance(true);

	protected static final Pattern YEAR_PATTERN = Pattern.compile("\\d{4}");
	protected static final Pattern DC_ISBN_PATTERN = Pattern
			.compile("isbn:(.*)");
	protected static final Pattern DC_ISSN_PATTERN = Pattern
			.compile("issn:(*)");

	public MetadataDublinCoreRecord(DublinCoreRecord dcRecord) {
		if (dcRecord == null) {
			throw new IllegalArgumentException(
					"Creating MetadataDublinCoreRecord with NULL underlying dcRecord.");
		}
		this.dcRecord = dcRecord;
	}

	@Override
	public List<String> getTitle() {
		return dcRecord.getTitles();
	}

	@Override
	public String getFormat() {
		List<String> type = dcRecord.getTypes();
		for (String f : type) {
			//Kramerius specific
			if (f.equals("model:monograph")) {
				return "Book";
			//Kramerius specific
			} else if (f.equals("model:periodical")) {
				return "Journal";
			}
		}
		return null;
	}

	@Override
	public Long getPublicationYear() {
		// expecting year in date (should work for Kramerius)
		String year = dcRecord.getFirstDate();
		if (year == null) {
			return null;
		}
		Matcher matcher = YEAR_PATTERN.matcher(year);
		try {
			if (matcher.find()) {
				return Long.parseLong(matcher.group(0));
			}
		} catch (NumberFormatException e) {
		}
		return null;
	}

	@Override
	public String export(IOFormat iOFormat) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUniqueId() {
		// expecting unique id in first identifier
		return this.dcRecord.getFirstIdentifier();
	}

	@Override
	public List<Isbn> getISBNs() {
		/*
		 * go through all identifiers look for isbn:* validate isbn return isbn
		 * list
		 */
		List<String> identifiers = dcRecord.getIdentifiers();
		List<Isbn> isbns = new ArrayList<Isbn>();
		Isbn isbn = new Isbn();
		Pattern p = DC_ISBN_PATTERN;
		Matcher m;
		Long isbnCounter = 0L;

		for (String f : identifiers) {

			m = p.matcher(f);
			if (m.find()) {
				try {
					String isbnStr = m.group(1);
					isbnStr = isbnValidator.validate(isbnStr);
					isbn.setIsbn(Long.valueOf(isbnStr));

					isbn.setNote("");
					isbn.setOrderInRecord(++isbnCounter);

					isbns.add(isbn);
				} catch (NumberFormatException nfe) {
					logger.info(String.format("Invalid ISBN: %s", m.group(1)));
					continue;
				}
			}
		}
		return isbns;
	}

	@Override
	public List<String> getISSNs() {
		List<String> identifiers = dcRecord.getIdentifiers();
		List<String> issns = new ArrayList<String>();
		Pattern p = DC_ISSN_PATTERN;
		Matcher m;

		for (String f : identifiers) {
			m = p.matcher(f);
			if (m.find()) {
				String issn = m.group(1);
				System.out.println("nalezeno issn: " + issn);
				issns.add(issn);
			}
		}
		return issns;
	}

	@Override
	public String getSeriesISSN() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getPageCount() {
		// TODO Auto-generated method stub
		return null;
	}

	protected boolean isBook() {
		List<String> type = dcRecord.getTypes();
		for (String f : type) {
			// Kramerius specific
			if (f.equals("model:monograph")) {
				return true;
			}
		}
		return false;
	}

	protected boolean isPeriodical() {
		List<String> type = dcRecord.getTypes();
		for (String f : type) {
			// Kramerius specific
			if (f.equals("model:periodical")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<HarvestedRecordFormat> getDetectedFormatList() {

		List<HarvestedRecordFormat> hrf = new ArrayList<HarvestedRecordFormat>();

		if (isBook())
			hrf.add(HarvestedRecordFormat.BOOKS);
		if (isPeriodical())
			hrf.add(HarvestedRecordFormat.PERIODICALS);

		return null;
	}

}
