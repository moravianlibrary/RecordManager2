package cz.mzk.recordmanager.server.marc.marc4j;

import cz.mzk.recordmanager.server.util.RecordUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.marc4j.MarcReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MunipressCsvStreamReader implements MarcReader {

	private Record record;
	private MarcFactory factory = new MarcFactoryImpl();

	private Iterator<CSVRecord> iterator;

	private static final String HEADER_ID = "id";
	private static final String HEADER_FAKULTA = "fakulta";
	private static final String HEADER_VYDAVATEL = "vydavatel";
	private static final String HEADER_NAZEV_ORIG = "nazev_original";
	private static final String HEADER_PODNAZEV = "podnazev";
	private static final String HEADER_NAZEV_CZ = "nazev_cz";
	private static final String HEADER_NAZEV_EN = "nazev_en";
	private static final String HEADER_PODNAZEV_CZ = "podnazev_cz";
	private static final String HEADER_PODNAZEV_EN = "podnazev_en";
	private static final String HEADER_ISBN_TISK = "isbn_tistena_verze";
	private static final String HEADER_ISBN_EL = "isbn_el_formaty";
	private static final String HEADER_ROK_EL = "rok_vydani_el_verze";
	private static final String HEADER_ROK_TISK = "rok_vydani_tistena";
	private static final String HEADER_ABSTRAKT = "abstrakt";
	private static final String HEADER_ABSTRAKT_EN = "abstrakt_en";
	private static final String HEADER_OBORY = "obory";
	private static final String HEADER_JAZYKY = "jazyky";
	private static final String HEADER_EDICE = "edice";
	private static final String HEADER_CISLO_SVAZKU = "cislo_svazku";
	private static final String HEADER_TYP_PUBLIKACE = "typ_publikace";
	private static final String HEADER_ZANR = "zanr";
	private static final String HEADER_ISBN_KOEDICE = "isbn_koedice";
	private static final String HEADER_KOEDICE = "koedice";
	private static final String HEADER_ISSN_PRINT = "issn_print";
	private static final String HEADER_ISSN_ONLINE = "issn_online";
	private static final String HEADER_DOI = "doi";
	private static final String HEADER_PORADI_VYDANI = "poradi_vydani";
	private static final String HEADER_KEYWORDS = "keywords_cz";
	private static final String HEADER_KEYWORDS_EN = "keywords_en";
	private static final String HEADER_ODKAZ = "odkaz_munispace";
	private static final String HEADER_AUTOR_X = "autor_%d";

	private static final String TEXT_500A = "Tato metadata vznikla konverzí z dat, která nebyla vytvořena dle " +
			"knihovnických standardů.";

	private static final int MAX_AUTHOR = 33;
	private List<String> authors_260c;

	private static final String DATE_STRING_005 = "yyyyMMddHHmmss'.0'";
	private static final SimpleDateFormat SDF_005 = new SimpleDateFormat(DATE_STRING_005);
	private static final String FORMAT_008 = "191007s%s||||xr||||||||||||||||||%s||";

	private static final Pattern PATTERN_AUTHOR = Pattern.compile("\\s*([^\\s]+)\\s+([^\\s]+)\\s*(\\(ed\\.\\))?", Pattern.CASE_INSENSITIVE);
	private static final Pattern PATTERN_AUTHOR_MORE = Pattern.compile("(.+)\\$(.+)\\s*(\\(ed\\.\\))?", Pattern.CASE_INSENSITIVE);
	private static final Pattern PATTERN_COMMA = Pattern.compile(",", Pattern.CASE_INSENSITIVE);

	/**
	 * Constructs an instance with the specified input stream.
	 */
	public MunipressCsvStreamReader(InputStream input) {
		initializeReader(input);
	}

	private void initializeReader(InputStream input) {
		try {
			CSVParser parser = new CSVParser(new StringReader(IOUtils.toString(input, StandardCharsets.UTF_8)),
					CSVFormat.EXCEL.withHeader());
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
		authors_260c = new ArrayList<>();
		CSVRecord csv = iterator.next();
		createLeader();
		createId(csv.get(HEADER_ID));
		create005();
		create008(csv.get(HEADER_ROK_TISK).isEmpty() ? csv.get(HEADER_ROK_EL) : csv.get(HEADER_ROK_TISK),
				csv.get(HEADER_JAZYKY));
		create020(csv.get(HEADER_ISBN_TISK));
		create020(csv.get(HEADER_ISBN_EL));
		create041(csv.get(HEADER_JAZYKY));
		createAuthor("100", csv.get(String.format(HEADER_AUTOR_X, 1)));
		int i = 2;
		while (i <= MAX_AUTHOR) {
			String author = csv.get(String.format(HEADER_AUTOR_X, i));
			if (author.isEmpty()) break;
			createAuthor("700", author);
			i++;
		}
		create245(csv.get(HEADER_NAZEV_ORIG), csv.get(HEADER_PODNAZEV));
		create250(csv.get(HEADER_PORADI_VYDANI));
		create264(csv.get(HEADER_ROK_TISK).isEmpty() ? csv.get(HEADER_ROK_EL) : csv.get(HEADER_ROK_TISK));
		addDataField("336", ' ', ' ', "a", "text", "b", "txt", "2", "rdacontent");
		addDataField("337", ' ', ' ', "a", csv.get(HEADER_ISBN_TISK).isEmpty() ? "počítač" : "bez média", "b", "n", "2", "rdamedia");
		create490(csv.get(HEADER_EDICE), csv.get(HEADER_CISLO_SVAZKU));
		addDataField("338", ' ', ' ', "a", csv.get(HEADER_ISBN_TISK).isEmpty() ? "online zdroj" : "svazek", "b", "nc", "2", "rdacarrier");
		if (!csv.get(HEADER_ABSTRAKT).isEmpty()) addDataField("520", ' ', ' ', "a", csv.get(HEADER_ABSTRAKT));
		addDataField("590", ' ', ' ', "a", TEXT_500A);
		if (!csv.get(HEADER_KEYWORDS).isEmpty()) addDataField("653", ' ', ' ', "a", csv.get(HEADER_KEYWORDS));
		create650(csv.get(HEADER_OBORY));
		if (!csv.get(HEADER_ZANR).isEmpty()) addDataField("655", '7', ' ', "a", csv.get(HEADER_ZANR));
		if (!csv.get(HEADER_TYP_PUBLIKACE).isEmpty()) addDataField("655", '7', ' ', "a", csv.get(HEADER_TYP_PUBLIKACE));
		if (!csv.get(HEADER_ODKAZ).isEmpty())
			addDataField("856", '4', '1', "u", csv.get(HEADER_ODKAZ), "y", "Plný text");
		addDataField("710", ' ', '2', "a", "Masarykova univerzita", "7", "kn20010709056", "4", "pbl");
		return RecordUtils.sortFields(record);
	}

	private void addDataField(String tag, char ind1, char ind2, String... subfieldCodesAndData) {
		record.addVariableField(factory.newDataField(tag, ind1, ind2, subfieldCodesAndData));
	}

	private void createLeader() {
		record.setLeader(factory.newLeader("02749nam a2200649 i 4500"));
	}

	private void createId(final String id) {
		record.addVariableField(factory.newControlField("001", id));
	}

	private void create005() {
		record.addVariableField(factory.newControlField("005", SDF_005.format(new Date())));
	}

	private void create008(final String year, final String lang) {
		String localYear = year.isEmpty() ? StringUtils.repeat('|', 4) : year;
		String localLang = lang.isEmpty() ? StringUtils.repeat('|', 3) : lang.split(",")[0];
		record.addVariableField(factory.newControlField("008", String.format(FORMAT_008, localYear, localLang)));
	}

	private void create020(final String isbns) {
		if (isbns.isEmpty()) return;
		for (String isbn : isbns.split(",")) {
			addDataField("020", ' ', ' ', "a", isbn);
		}
	}

	private void create041(final String langs) {
		if (langs.split(",").length <= 1) return;
		DataField df = factory.newDataField("041", ' ', ' ');
		for (String lang : langs.split(",")) {
			df.addSubfield(factory.newSubfield('a', lang.trim()));
		}
		record.addVariableField(df);
	}

	private void createAuthor(final String tag, final String author) {
		if (author.isEmpty()) return;
		DataField df = factory.newDataField(tag, '1', ' ');
		Matcher matcher;
		matcher = author.contains("$") ? PATTERN_AUTHOR_MORE.matcher(author) : PATTERN_AUTHOR.matcher(author);
		if (matcher.find()) {
			df.addSubfield(factory.newSubfield('a', matcher.group(2).trim() + ", " + matcher.group(1).trim()));
			df.addSubfield(factory.newSubfield('4', matcher.group(3) == null ? "aut" : "edt"));
			authors_260c.add(matcher.group(1).trim() + " " + matcher.group(2).trim());
		}
		record.addVariableField(df);
	}

	private void create245(final String title_orig, final String subtitle) {
		DataField df = factory.newDataField("245", '1', '0');
		if (!title_orig.isEmpty()) df.addSubfield(factory.newSubfield('a', title_orig + " :"));
		if (!subtitle.isEmpty()) df.addSubfield(factory.newSubfield('b', subtitle + " /"));
		if (!authors_260c.isEmpty()) df.addSubfield(factory.newSubfield('c', String.join(", ", authors_260c)));
		record.addVariableField(df);
	}

	private void create250(final String edition) {
		if (edition.isEmpty()) return;
		addDataField("250", ' ', ' ', "a", edition);
	}

	private void create264(final String year) {
		if (year.isEmpty()) return;
		addDataField("264", '1', ' ', "a", "Brno",
				"b", "Masarykova univerzita", "c", year);
	}

	private void create490(final String edition, final String number) {
		if (edition.isEmpty()) return;
		DataField df = factory.newDataField("490", '1', ' ');
		df.addSubfield(factory.newSubfield('a', edition + " ;"));
		if (!number.isEmpty()) df.addSubfield(factory.newSubfield('v', number));
		record.addVariableField(df);
	}

	private void create650(final String conspectus) {
		if (conspectus.isEmpty()) return;
		for (String value : conspectus.split(";")) {
			record.addVariableField(factory.newDataField("650", '0', '7', "a", value.trim().toLowerCase()));
		}
	}

}
