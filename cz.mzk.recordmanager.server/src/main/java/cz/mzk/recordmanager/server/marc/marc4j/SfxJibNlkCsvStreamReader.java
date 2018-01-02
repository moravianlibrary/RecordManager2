package cz.mzk.recordmanager.server.marc.marc4j;

import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.mzk.recordmanager.server.util.RecordUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.validator.routines.ISBNValidator;
import org.marc4j.MarcReader;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

public class SfxJibNlkCsvStreamReader implements MarcReader {

	private Record record;
	private MarcFactory factory = new MarcFactoryImpl();
	private final ISBNValidator isbnValidator = ISBNValidator.getInstance(true);
	protected static final String ISBN_CLEAR_REGEX = "[^0-9^X^x]";

	private static final String TEXT_LEADER_MONOGRAPH = "00710nam a2200229   4500";
	private static final String TEXT_LEADER_SERIAL = "00710nas a2200229   4500";
	private static final String TEXT_008 = "160323s%sxr     g      000 f cze";
	private static final String DATE_STRING_005 = "yyyyMMddHHmmss'.0'";

	private static final Pattern PREFIX = Pattern.compile(
			"^((?:der|die|das|the|an) ).*", Pattern.CASE_INSENSITIVE);
	private static final Pattern INSTITUTION = Pattern.compile("sfxjib(.*)");
	private static final Pattern YEAR = Pattern.compile("\\d{4}");
	private String idPrefix;
	private Iterator<CSVRecord> iterator;
	private CSVParser parser;
	private AtomicInteger id = new AtomicInteger(0);
	/**
	 * Constructs an instance with the specified input stream.
	 */
	public SfxJibNlkCsvStreamReader(InputStream input, String idPrefix) {
		this.idPrefix = idPrefix;
		initializeReader(input);
	}

	private void initializeReader(InputStream input) {
		try {
			parser = new CSVParser(new StringReader(IOUtils.toString(input, StandardCharsets.UTF_8)), CSVFormat.EXCEL);
			iterator = parser.iterator();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns true if the iteration has more records, false otherwise.
	 */
	public boolean hasNext() {
		return iterator.hasNext();
	}

	/**
	 * Returns the next record in the iteration.
	 * 
	 * @return Record - the record object
	 */
	public Record next() {
		record = factory.newRecord();
		SfxNlkRecord sfx = new SfxNlkRecord(iterator.next());
		record.setLeader(factory.newLeader(sfx.isBook() ? TEXT_LEADER_MONOGRAPH : TEXT_LEADER_SERIAL));
		record.addVariableField(factory.newControlField("001", String.valueOf(id.getAndIncrement())));
		Matcher matcher = INSTITUTION.matcher(idPrefix);
		if (matcher.matches()) {
			record.addVariableField(factory.newControlField("003", matcher.group(1)));
		}
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_STRING_005);
		record.addVariableField(factory.newControlField("005", sdf.format(new Date())));
		record.addVariableField(factory.newControlField("008", String.format(
						TEXT_008, (sfx.getFrom() == null ? "    " : sfx.getFrom())
								+ (sfx.getTo() == null ? (sfx.isBook() ? "    "	: "9999") : sfx.getTo()))));
		if (sfx.getIsbns() != null) sfx.getIsbns().forEach(i -> addDataField("020", ' ', ' ', "a", i));
		if (sfx.getIssns() != null) sfx.getIssns().forEach(i -> addDataField("022", ' ', ' ', "a", i));
		if (sfx.getAuthor() != null) addDataField("100", '1', '#', "a", sfx.getAuthor());
		if (sfx.getTitle() != null) {
			char ind2 = '0';
			if ((matcher = PREFIX.matcher(sfx.getTitle())).matches()) {
				ind2 = Character.forDigit(matcher.group(1).length(), 10);
			}
			addDataField("245", '1', ind2, "a", sfx.getTitle());
		}
		if (sfx.getType() != null) addDataField("300", ' ', ' ', "a", sfx.getType());
		if (sfx.getUrl() != null) addDataField("856", ' ', ' ', "u", sfx.getUrl());
		addYears(sfx);
		RecordUtils.sortFields(record);
		return record;
	}

	private void addDataField(String tag, char ind1, char ind2, String... subfieldCodesAndData) {
		record.addVariableField(factory.newDataField(tag, ind1, ind2, subfieldCodesAndData));
	}

	private void addYears(SfxNlkRecord sfx) {
		Set<String> years = new HashSet<>();
		if (sfx.getYear() != null) {
			addDataField("260", ' ', ' ', "c", sfx.getYear());
			years.add(sfx.getYear());
		} else if (sfx.getFrom() != null) {
			generateYears(years, sfx.getFrom(), sfx.getTo());
			addDataField("260", ' ', ' ', "c", sfx.getFrom() + "-" + (sfx.getTo() == null ? "" : sfx.getTo()));
		}
		generateFields996(years);
	}

	private Set<String> generateYears(Set<String> years, String fromStr,
			String toStr) {
		if (fromStr == null) return years;
		int from = Integer.valueOf(fromStr);
		int to = (toStr == null) ? Calendar.getInstance().get(Calendar.YEAR)
				: Integer.valueOf(toStr);

		for (int i = from; i <= to; i++) {
			years.add(String.valueOf(i));
		}
		return years;
	}

	private void generateFields996(Set<String> years) {
		years.forEach(y -> addDataField("996", ' ', ' ', "y", y));
	}

	protected class SfxNlkRecord {

		private String type;
		private String title;
		private String issn;
		private String eissn;
		private String isbn10;
		private String isbn13;
		private String from;
		private String to;
		private String year;
		private String resource;
		private String subject;
		private String url;
		private String author;

		public SfxNlkRecord(CSVRecord csv) {
			this.type = csv.get(0);
			this.title = csv.get(1);
			this.issn = csv.get(2);
			this.eissn = csv.get(3);
			this.isbn10 = csv.get(4);
			this.isbn13 = csv.get(5);
			this.from = csv.get(6);
			this.to = csv.get(7);
			this.year = csv.get(8);
			this.resource = csv.get(9);
			this.subject = csv.get(10);
			this.url = csv.get(11);
			this.author = csv.get(12);
		}

		public String getType() {
			return type;
		}

		public boolean isBook() {
			return type.equals("Book");
		}

		public String getTitle() {
			return title.equals("") ? null : title;
		}

		public List<String> getIssns() {
			List<String> issns = new ArrayList<>();
			if (!eissn.equals("")) issns.add(eissn);
			if (!issn.equals("") && !issns.contains(issn)) issns.add(issn);
			return issns.isEmpty() ? null : issns;
		}

		public List<String> getIsbns() {
			List<String> isbns = new ArrayList<>();
			if (!isbn10.equals("")) {
				String isbn10Str = isbnValidator.validate(isbn10.replaceAll(
						ISBN_CLEAR_REGEX, "").replaceAll("x", "X"));
				if (isbn10Str != null) isbns.add(isbn10Str);
			}
			if (!isbn13.equals("") && !isbns.contains(isbn13)) isbns.add(isbn13);
			return isbns.isEmpty() ? null : isbns;
		}

		public String getFrom() {
			Matcher matcher = YEAR.matcher(from);
			if (matcher.find()) return matcher.group(0);
			return null;
		}

		public String getTo() {
			Matcher matcher = YEAR.matcher(to);
			if (matcher.find()) return matcher.group(0);
			return null;
		}

		public String getYear() {
			return year.equals("") ? null : year;
		}

		public String getResource() {
			return resource.equals("") ? null : resource;
		}

		public String getSubject() {
			return subject.equals("") ? null : subject;
		}

		public String getUrl() {
			return url.equals("") ? null : url;
		}

		public String getAuthor() {
			return author.equals("") ? null : author;
		}

		@Override
		public String toString() {
			return "SfxNlkRecord [type=" + type + ", title=" + title
					+ ", issn=" + issn + ", eissn=" + eissn + ", isbn10="
					+ isbn10 + ", isbn13=" + isbn13 + ", from=" + from
					+ ", to=" + to + ", year=" + year + ", resource="
					+ resource + ", subject=" + subject + ", url=" + url
					+ ", author=" + author + "]";
		}
	}
}
