package cz.mzk.recordmanager.server.marc.marc4j;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import cz.mzk.recordmanager.server.util.constants.SfxConstants;
import cz.mzk.recordmanager.server.util.RecordUtils;
import org.marc4j.MarcReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

public class SfxJibXmlStreamReader implements MarcReader {

	private Record record;

	private MarcFactory factory;

	private XMLInputFactory xmlFactory;

	private XMLStreamReader xmlReader;

	private static final String TEXT_LEADER_MONOGRAPH = "00710nam a2200229   4500";
	private static final String TEXT_LEADER_SERIAL = "00710nas a2200229   4500";
	private static final String TEXT_008 = "160323s%sxr     g      000 f cze";
	private static final String TEXT_EMBARGO_NOT_AVAILABLE = "embargo_not_available ";
	private static final String TEXT_EMBARGO_AVAILABLE = "embargo_available ";
	private static final String DATE_STRING_005 = "yyyyMMddHHmmss'.0'";

	private static final Pattern PREFIX = Pattern.compile(
			"^((?:der|die|das|the|an) ).*", Pattern.CASE_INSENSITIVE);
	private static final Pattern INSTITUTION = Pattern.compile("sfxjib(.*)");
	private static final List<String> SERIALS = Arrays.asList("JOURNAL",
			"NEWSPAPER", "SERIES", "TRANSCRIPT", "WIRE");
	private String idPrefix;

	/**
	 * Constructs an instance with the specified input stream.
	 */
	public SfxJibXmlStreamReader(InputStream input, String idPrefix) {
		this.idPrefix = idPrefix;
		xmlFactory = XMLInputFactory.newInstance();
		factory = MarcFactoryImpl.newInstance();
		initializeReader(input);
	}

	private void initializeReader(InputStream input) {
		try {
			xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
			this.xmlReader = xmlFactory.createXMLStreamReader(input);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns true if the iteration has more records, false otherwise.
	 */
	public boolean hasNext() {
		try {
			return xmlReader.hasNext();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Returns the next record in the iteration.
	 *
	 * @return Record - the record object
	 */
	public Record next() {
		record = null;
		List<DataField> isbns = new ArrayList<>();
		List<DataField> issns = new ArrayList<>();
		String coverage = null;
		String from = null;
		int minFrom = Integer.MAX_VALUE;
		String to = null;
		int maxTo = 0;
		String embargo = null;
		String type = null;
		Set<String> years = new TreeSet<>();
		int volumeFirstYear = 0;
		int volumeYear = 0;
		try {
			while (xmlReader.hasNext()) {
				switch (xmlReader.getEventType()) {
				case XMLStreamReader.START_ELEMENT:
					switch (xmlReader.getLocalName()) {
					case SfxConstants.ELEMENT_ITEM:
						record = factory.newRecord();
						addFields();
						break;
					case SfxConstants.ELEMENT_SFX_ID:
						record.addVariableField(factory.newControlField("001",
								xmlReader.getElementText()));
						break;
					case SfxConstants.ELEMENT_ISSN:
						issns.add(getIsxnField("022"));
						break;
					case SfxConstants.ELEMENT_EISSN:
						issns.add(0, getIsxnField("022"));
						break;
					case SfxConstants.ELEMENT_ISBN:
						isbns.add(getIsxnField("020"));
						break;
					case SfxConstants.ELEMENT_EISBN:
						isbns.add(0, getIsxnField("020"));
						break;
					case SfxConstants.ELEMENT_OBJECT_TYPE:
						type = xmlReader.getElementText();
						record.addVariableField(factory.newDataField("300",
								' ', ' ', "a", type));
						break;
					case SfxConstants.ELEMENT_AUTHOR:
						if (record.getVariableField("100") == null) {
							record.addVariableField(factory.newDataField("100",
									'1', '#', "a", xmlReader.getElementText()));
						} else {
							record.addVariableField(factory.newDataField("700",
									'1', '0', "a", xmlReader.getElementText()));
						}
						break;
					case SfxConstants.ELEMENT_TITLE:
						if (record.getVariableField("245") == null) {
							String data = xmlReader.getElementText();
							Matcher matcher;
							char ind2 = '0';
							if ((matcher = PREFIX.matcher(data)).matches()) {
								ind2 = Character.forDigit(matcher.group(1).length(), 10);
							}
							record.addVariableField(factory.newDataField("245",
									'1', ind2, "a", data));
						} else {
							record.addVariableField(factory.newDataField("246",
									'0', '3', "a", xmlReader.getElementText()));
						}
						break;
					case SfxConstants.ELEMENT_DAYS_AVAILABLE:
						String available = xmlReader.getElementText();
						if (record.getVariableField("500") == null) {
							record.addVariableField(factory.newDataField("500",
									' ', ' ', "a", TEXT_EMBARGO_AVAILABLE + available));
						}
						embargo = 'a' + available;
						break;
					case SfxConstants.ELEMENT_DAYS_NOT_AVAILABLE:
						String notAvailable = xmlReader.getElementText();
						if (record.getVariableField("500") == null) {
							record.addVariableField(factory.newDataField("500",
									' ', ' ', "a", TEXT_EMBARGO_NOT_AVAILABLE + notAvailable));
						}
						embargo = 'n' + notAvailable;
						break;
					case SfxConstants.ELEMENT_FROM:
						coverage = "from";
						break;
					case SfxConstants.ELEMENT_TO:
						coverage = "to";
						break;
					case SfxConstants.ELEMENT_YEAR:
						String year = xmlReader.getElementText();
						if (coverage != null) {
							if (coverage.equals("from")) {
								from = year;
							} else
								to = year;
						}
						volumeYear = (year != null) ? Integer.valueOf(year) : 0;
						break;
					case SfxConstants.ELEMENT_VOLUME:
						if (volumeFirstYear < 0) { // error
							break;
						}
						String volumeStr = xmlReader.getElementText();
						if (volumeStr != null && volumeYear != 0) {
							int volumeFirstYearLocal = volumeYear - Integer.valueOf(volumeStr) + 1;
							if (volumeFirstYear == 0) {
								volumeFirstYear = volumeFirstYearLocal; // new first year
							} else if (volumeFirstYear != volumeFirstYearLocal) {
								volumeFirstYear = -1; // error
							}
						}
						break;
					}
					break;
				case XMLStreamReader.END_ELEMENT:
					switch (xmlReader.getLocalName()) {
					case SfxConstants.ELEMENT_ITEM:
						while (xmlReader.hasNext()
								&& xmlReader.getEventType() != XMLStreamReader.START_ELEMENT) {
							xmlReader.next();
						}
						isbns.forEach(df -> record.addVariableField(df));
						issns.forEach(df -> record.addVariableField(df));
						String year008 = "        ";
						String year260;
						if (minFrom != Integer.MAX_VALUE) {
							if (SERIALS.contains(type)) {
								year260 = String.valueOf(minFrom)
										+ '-' + ((minFrom < maxTo) ? String.valueOf(maxTo) : "");
								year008 = String.valueOf(minFrom) + ((maxTo == 0) ? "9999" : maxTo);
							} else {
								year260 = String.valueOf(minFrom);
								year008 = String.valueOf(minFrom) + "    ";
							}
							record.addVariableField(factory.newDataField("260",
									' ', ' ', "c", year260));
						}
						record.addVariableField(factory.newControlField("008",
								String.format(TEXT_008, year008)));
						record.setLeader(factory.newLeader(SERIALS.contains(type) ? TEXT_LEADER_SERIAL
								: TEXT_LEADER_MONOGRAPH));
						generateFields996(years, volumeFirstYear);
						return RecordUtils.sortFields(record);
					case SfxConstants.ELEMENT_FROM:
					case SfxConstants.ELEMENT_TO:
						coverage = null;
						break;
					case SfxConstants.ELEMENT_COVERAGE:
						try {
							if (Integer.valueOf(from) < minFrom) {
								minFrom = Integer.valueOf(from);
							}
							if (maxTo < Integer.valueOf(to)) {
								maxTo = Integer.valueOf(to);
							}
						} catch (Exception ignore) {
						}
						generateYears(years, from, to, embargo);
						record.addVariableField(generateCoverageField(from, to, embargo));
						from = to = embargo = null;
						break;
					}
					break;
				}
				xmlReader.next();
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		return record;
	}

	private void addFields() {
		Matcher matcher = INSTITUTION.matcher(idPrefix);
		if (matcher.matches()) {
			record.addVariableField(factory.newControlField("003", matcher.group(1)));
		}
	}

	private DataField getIsxnField(String tag) throws XMLStreamException {
		return factory.newDataField(tag, ' ', ' ', "a", xmlReader.getElementText());
	}

	private void generateYears(Set<String> years, String fromStr, String toStr, String embargo) {
		int from;
		int to;
		if (embargo == null) {
			if (fromStr == null) return;
			from = Integer.valueOf(fromStr);
			to = (toStr == null) ? Calendar.getInstance().get(Calendar.YEAR)
					: Integer.valueOf(toStr);
		} else {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_YEAR, -Integer.valueOf(embargo.substring(1)));
			if (embargo.startsWith("a")) {
				from = cal.get(Calendar.YEAR);
				to = Calendar.getInstance().get(Calendar.YEAR);
			} else {
				if (fromStr == null) {
					from = cal.get(Calendar.YEAR);
				} else {
					from = Integer.valueOf(fromStr);
				}
				to = cal.get(Calendar.YEAR);
			}
		}

		for (int i = from; i <= to; i++) {
			years.add(String.valueOf(i));
		}
	}

	private void generateFields996(Set<String> years, int volumeFirstYear) {
		years.forEach(y -> {
			DataField df = factory.newDataField("996", ' ', ' ', "y", y);
			if (volumeFirstYear > 0) {
				df.addSubfield(factory.newSubfield('v', String.valueOf(
						Integer.valueOf(y) - volumeFirstYear + 1)));
			}
			record.addVariableField(df);
		});
	}

	private DataField generateCoverageField(String from, String to, String embargo) {
		DataField df = factory.newDataField("COV", ' ', ' ');
		if (from != null) df.addSubfield(factory.newSubfield('a', from));
		if (to != null) df.addSubfield(factory.newSubfield('b', to));
		if (embargo != null) df.addSubfield(factory.newSubfield('c', embargo));
		return df;
	}

}
