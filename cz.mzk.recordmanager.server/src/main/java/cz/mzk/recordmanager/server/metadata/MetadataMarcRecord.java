package cz.mzk.recordmanager.server.metadata;

import com.google.common.primitives.Chars;
import cz.mzk.recordmanager.server.ClasspathResourceProvider;
import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.model.*;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum;
import cz.mzk.recordmanager.server.model.TezaurusRecord.TezaurusKey;
import cz.mzk.recordmanager.server.util.*;
import cz.mzk.recordmanager.server.util.identifier.*;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MetadataMarcRecord implements MetadataRecord {

	private static Logger logger = LoggerFactory.getLogger(MetadataMarcRecord.class);

	protected MarcRecord underlayingMarc;

	protected HarvestedRecord harvestedRecord;

	private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+)");
	private static final Pattern YEAR_PATTERN = Pattern.compile("\\d{4}");
	private static final Pattern SCALE_PATTERN = Pattern.compile("\\d+[ ^]*\\d+");
	protected static final Pattern UUID_PATTERN = Pattern.compile("uuid:[\\w-]+");
	private static final Pattern OCLC_PATTERN = Pattern.compile("(\\(ocolc\\))(.+)", Pattern.CASE_INSENSITIVE);
	private static final Pattern PUBLISHER_NUMBER_PATTERN = Pattern.compile("\\W");
	private static final Pattern CPK0_PATTERN = Pattern.compile("cpk0");
	private static final Pattern METAPROXY_TAG_PATTERN = Pattern.compile("[17]..");
	private static final Pattern SCALE_REPLACE = Pattern.compile("[ ^]+");

	// formats
	private static final Pattern KARTOGRAFICKY_DOKUMENT = Pattern.compile("kartografick[yý]\\sdokument", Pattern.CASE_INSENSITIVE);
	private static final Pattern START_CR = Pattern.compile("^cr", Pattern.CASE_INSENSITIVE);
	private static final Pattern HUDEBNINA = Pattern.compile("hudebnina", Pattern.CASE_INSENSITIVE);
	private static final Pattern GRAFIKA = Pattern.compile("grafika", Pattern.CASE_INSENSITIVE);
	private static final Pattern START_G = Pattern.compile("^g", Pattern.CASE_INSENSITIVE);
	private static final Pattern MIKRODOKUMENT = Pattern.compile("mikrodokument", Pattern.CASE_INSENSITIVE);
	private static final Pattern START_H = Pattern.compile("^h", Pattern.CASE_INSENSITIVE);
	private static final Pattern HMATOVE_PISMO = Pattern.compile("hmatov[eé]\\sp[ií]smo", Pattern.CASE_INSENSITIVE);
	private static final Pattern MACAN = Pattern.compile("macan", Pattern.CASE_INSENSITIVE);
	private static final Pattern KTN = Pattern.compile("ktn", Pattern.CASE_INSENSITIVE);
	private static final Pattern ELEKTRONICKY_ZDROJ = Pattern.compile("elektronick[yý]\\szdroj", Pattern.CASE_INSENSITIVE);
	private static final Pattern MULTIMEDIUM = Pattern.compile("multim[eé]dium", Pattern.CASE_INSENSITIVE);
	private static final Pattern CD_ROM = Pattern.compile("cd-rom", Pattern.CASE_INSENSITIVE);
	private static final Pattern DISKETA = Pattern.compile("disketa", Pattern.CASE_INSENSITIVE);
	private static final Pattern COMP_CARRIER_338B = Pattern.compile("ck|cb|cd|ce|ca|cf|ch|cz", Pattern.CASE_INSENSITIVE);
	private static final Pattern DVD = Pattern.compile("dvd", Pattern.CASE_INSENSITIVE);
	private static final Pattern KOMPAKTNI_DISK = Pattern.compile("kompaktn[ií](?:ch)?\\sd[ei]sk[uů]?", Pattern.CASE_INSENSITIVE);
	private static final Pattern ZVUKOVE_CD = Pattern.compile("zvukov[eéaá]\\sCD", Pattern.CASE_INSENSITIVE);
	private static final Pattern CD = Pattern.compile("CD", Pattern.CASE_INSENSITIVE);
	private static final Pattern CD_R = Pattern.compile("CD-R", Pattern.CASE_INSENSITIVE);
	private static final Pattern ZVUKOVA_DESKA = Pattern.compile("zvukov(?:[aáeé]|ych|ých)\\sdes(?:ka|ky|ek)", Pattern.CASE_INSENSITIVE);
	protected static final Pattern DIGITAL_OR_12CM = Pattern.compile("digital|12\\s*cm", Pattern.CASE_INSENSITIVE);
	private static final Pattern GRAMOFONOVA_DESKA = Pattern.compile("gramofonov(?:[aáeé]|ych|ých)\\sdes(?:ka|ky|ek)", Pattern.CASE_INSENSITIVE);
	private static final Pattern ANALOG = Pattern.compile("analog", Pattern.CASE_INSENSITIVE);
	private static final Pattern LP_OR_SP = Pattern.compile("LP|SP");
	private static final Pattern ZVUKOVA_KAZETA = Pattern.compile("zvukov(?:a|á|e|é|ych|ých)\\skaze(?:ta|ty|t)", Pattern.CASE_INSENSITIVE);
	private static final Pattern MAGNETOFONOVA_KAZETA = Pattern.compile("magnetofonov(?:a|á|e|é|ych|ých)\\skaze(?:ta|ty|t)", Pattern.CASE_INSENSITIVE);
	private static final Pattern ZVUKOVY_ZAZNAM = Pattern.compile("zvukov[yý]\\sz[aá]znam", Pattern.CASE_INSENSITIVE);
	private static final Pattern START_S = Pattern.compile("^s", Pattern.CASE_INSENSITIVE);
	private static final Pattern MC_OR_KZ_MGK = Pattern.compile("mc|kz|mgk", Pattern.CASE_INSENSITIVE);
	private static final Pattern BLURAY = Pattern.compile("blu.*ray", Pattern.CASE_INSENSITIVE);
	private static final Pattern VHS = Pattern.compile("vhs", Pattern.CASE_INSENSITIVE);
	private static final Pattern VIDEOKAZETA = Pattern.compile("videokazet[ay]?", Pattern.CASE_INSENSITIVE);
	private static final Pattern DVD_VIDEO = Pattern.compile("DVD[ -]?vide[oa]");
	private static final Pattern VIDEODISK = Pattern.compile("videodisk", Pattern.CASE_INSENSITIVE);
	private static final Pattern VIDEOZAZNAM = Pattern.compile("videoz[aá]znam", Pattern.CASE_INSENSITIVE);
	private static final Pattern START_V = Pattern.compile("^v", Pattern.CASE_INSENSITIVE);
	private static final Pattern VIDEO_OTHER_F338 = Pattern.compile("vr|vz|vc|mc|mf|mr|mo|mz");
	private static final Pattern OTHER_F336 = Pattern.compile("tcf|tdm|tdf", Pattern.CASE_INSENSITIVE);
	private static final Pattern FOTOGRAFIE = Pattern.compile("fotografie", Pattern.CASE_INSENSITIVE);

	private static final Long MAX_PAGES = 10_000_000L;
	private static final String INVALID_YEAR = "Invalid year: %s";
	private static final String[] TITLE_TAGS = {"245", "240"};
	private static final String[] AUTHORITY_ID_TAGS = {"100", "110", "111", "700", "710", "711"};
	private static final char[] SHORT_TITLE_SUBFIELDS = {'a', 'n', 'p'};
	private static final char[] TITLE_SUBFIELDS = {'a', 'b', 'n', 'p'};

	private static final char[] ARRAY_AT = {'a', 't'};
	private static final char[] ARRAY_CDM = {'c', 'd', 'm'};
	protected static final char[] ARRAY_IJ = {'i', 'j'};
	private static final char[] ARRAY_IS = {'i', 's'};
	private static final char[] ARRAY_AB = {'a', 'b'};
	private static final char[] ARRAY_EF = {'e', 'f'};
	private static final char[] ARRAY_CD = {'c', 'd'};
	private static final char[] ARRAY_KG = {'k', 'g'};
	private static final char[] ARRAY_ACDPT = {'a', 'c', 'd', 'p', 't'};
	private static final char[] ARRAY_ABC = {'a', 'b', 'c'};
	private static final char[] ARRAY_EFK = {'e', 'f', 'k'};
	private static final char[] ARRAY_ACDIJPT = {'a', 'c', 'd', 'i', 'j', 'p', 't'};
	private static final char[] ARRAY_EFGKOPR = {'e', 'f', 'g', 'k', 'o', 'p', 'r'};
	private static final char[] ARRAY_ZGEIGT = {'z', 'g', 'e', 'i', 'q', 't'};
	private static final char[] ARRAY_VM = {'v', 'm'};
	private static final char[] ARRAY_OPR = {'o', 'p', 'r'};
	private static final char[] ARRAY_OQ = {'o', 'q'};

	private static final String URL_COMMENT_FORMAT = "%s (%s)";

	private static final List<String> ANP_TITLE_REMOVE_WORDS = new BufferedReader(new InputStreamReader(
			new ClasspathResourceProvider().getResource("/list/anp_title_remove_words.txt"), StandardCharsets.UTF_8))
			.lines().collect(Collectors.toCollection(ArrayList::new));

	private static final List<Pattern> ANP_TITLE_REMOVE_WORDS_PATTERNS = ANP_TITLE_REMOVE_WORDS.stream()
			.map(w -> Pattern.compile("\\b" + w + "\\b", Pattern.CASE_INSENSITIVE)).collect(Collectors.toList());

	public MetadataMarcRecord(MarcRecord underlayingMarc) {
		initRecords(underlayingMarc, null);
	}

	public MetadataMarcRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		initRecords(underlayingMarc, hr);
	}

	private void initRecords(MarcRecord underlayingMarc, HarvestedRecord hr) {
		if (underlayingMarc == null) {
			throw new IllegalArgumentException(
					"Creating MetadataMarcRecord with NULL underlayingMarc.");
		}
		this.underlayingMarc = underlayingMarc;
		this.harvestedRecord = hr;
	}

	@Override
	public String getUniqueId() {
		String id = underlayingMarc.getField("OAI", 'a');
		if (id != null) return id;
		id = underlayingMarc.getControlField("001");
		if (id != null) return id;
		return underlayingMarc.getField("995", 'a');
	}

	@Override
	public List<Issn> getISSNs() {
		List<Issn> results = new ArrayList<>();
		Long issnCounter = 0L;
		Issn issn;

		for (DataField df : underlayingMarc.getDataFields("022")) {
			try {
				issn = ISSNUtils.createIssn(df);
			} catch (NoDataException nde) {
				continue;
			} catch (NumberFormatException nfe) {
				logger.info(String.format("Invalid ISSN: %s", nfe.getMessage()));
				continue;
			}
			issn.setOrderInRecord(++issnCounter);
			results.add(issn);
		}
		return results;
	}

	@Override
	public List<Cnb> getCNBs() {
		List<Cnb> cnbs = new ArrayList<>();

		for (DataField field : underlayingMarc.getDataFields("015")) {
			for (Subfield subfieldA : field.getSubfields('a')) {
				if (subfieldA != null) {
					cnbs.add(Cnb.create(subfieldA.getData()));
				}
			}
		}
		return cnbs;
	}

	@Override
	public String getISSNSeries() {
		String result = underlayingMarc.getField("490", 'x');
		if (result != null) {
			return result.substring(0, Math.min(result.length(), 300));
		}
		return null;
	}

	@Override
	public String getISSNSeriesOrder() {
		String result = underlayingMarc.getField("490", 'v');
		if (result != null) {
			return result.substring(0, Math.min(result.length(), 300));
		}
		return null;
	}

	@Override
	public Long getPageCount() {
		String count = underlayingMarc.getField("300", 'a');
		if (count == null) {
			return null;
		}

		Long maxPages = -1L;
		Matcher matcher = NUMBER_PATTERN.matcher(count);
		while (matcher.find()) {
			try {
				Long pages = Long.parseLong(matcher.group(0));
				maxPages = pages > maxPages ? pages : maxPages;
			} catch (NumberFormatException e) {
				logger.info(String.format(INVALID_YEAR, matcher.group(0)));
			}
		}

		if (maxPages < 1L) {
			return null;
		}

		return maxPages < MAX_PAGES ? maxPages : MAX_PAGES;
	}

	@Override
	public List<Isbn> getISBNs() {
		List<Isbn> isbns = new ArrayList<>();
		Long isbnCounter = 0L;
		Isbn isbn;

		for (DataField df : underlayingMarc.getDataFields("020")) {
			try {
				isbn = ISBNUtils.createIsbn(df);
			} catch (NoDataException nde) {
				continue;
			} catch (NumberFormatException nfe) {
				logger.info(String.format("Invalid ISBN: %s", nfe.getMessage()));
				continue;
			}
			isbn.setOrderInRecord(++isbnCounter);
			isbns.add(isbn);
		}
		return isbns;
	}

	/**
	 * get publication year from fields 264c, 260c or 008
	 */
	@Override
	public Long getPublicationYear() {

		String year = underlayingMarc.getField("264", 'c');
		if (year == null) {
			year = underlayingMarc.getField("260", 'c');
		}
		if (year == null) {
			year = underlayingMarc.getControlField("008");
			if (year == null || year.length() < 12) {
				return null;
			}
			year = year.substring(7, 11);
		}

		Matcher matcher = YEAR_PATTERN.matcher(year);
		try {
			if (matcher.find()) {
				return Long.parseLong(matcher.group(0));
			}
		} catch (NumberFormatException e) {
			logger.info(String.format(INVALID_YEAR, matcher.group(0)));
		}
		return null;
	}

	/**
	 * get {@link Title} of record
	 *
	 * @return all 245abnp and 240abnp
	 */
	@Override
	public List<Title> getTitle() {
		List<Title> result = new ArrayList<>();
		Long titleOrder = 0L;
		for (String key : TITLE_TAGS) {
			for (DataField df : underlayingMarc.getDataFields(key)) {
				String titleText = parseTitleValue(df, TITLE_SUBFIELDS);
				if (!titleText.isEmpty()) {
					result.add(Title.create(titleText, ++titleOrder, MetadataUtils.similarityEnabled(df, titleText)));
				}
			}
		}
		return result;
	}

	@Override
	public String export(IOFormat iOFormat) {
		return underlayingMarc.export(iOFormat);
	}

	protected boolean isBook() {
		char ldr06 = getLeaderChar(underlayingMarc.getLeader().getTypeOfRecord());
		char ldr07 = getLeaderChar(underlayingMarc.getLeader().getImplDefined1()[0]);

		String f006 = underlayingMarc.getControlField("006");
		char f006_00 = (f006 != null && !f006.isEmpty()) ? Character.toLowerCase(f006.charAt(0)) : ' ';

		return (MetadataUtils.containsChar(ARRAY_AT, ldr06) && MetadataUtils.containsChar(ARRAY_CDM, ldr07))
				|| f006_00 == 'a';
	}

	protected boolean isPeriodical() {
		char ldr07 = getLeaderChar(underlayingMarc.getLeader().getImplDefined1()[0]);
		return MetadataUtils.containsChar(ARRAY_IS, ldr07);
	}

	protected boolean isArticle() {
		char ldr07 = getLeaderChar(underlayingMarc.getLeader().getImplDefined1()[0]);
		return MetadataUtils.containsChar(ARRAY_AB, ldr07);
	}

	protected boolean isArticle773() {
		return !underlayingMarc.getDataFields("773").isEmpty();
	}

	protected boolean isMap() {
		char ldr06 = getLeaderChar(underlayingMarc.getLeader().getTypeOfRecord());

		String f006 = underlayingMarc.getControlField("006");
		char f006_00 = (f006 != null && !f006.isEmpty()) ? Character.toLowerCase(f006.charAt(0)) : ' ';

		String f007 = underlayingMarc.getControlField("007");
		char f007_00 = (f007 != null && !f007.isEmpty()) ? Character.toLowerCase(f007.charAt(0)) : ' ';

		String f245h = underlayingMarc.getField("245", 'h');
		if (f245h == null) f245h = "";

		String f336b = underlayingMarc.getField("336", 'b');
		if (f336b == null) f336b = "";

		return MetadataUtils.containsChar(ARRAY_EF, ldr06)
				|| MetadataUtils.containsChar(ARRAY_EF, f006_00)
				|| KARTOGRAFICKY_DOKUMENT.matcher(f245h).find()
				|| f007_00 == 'a'
				|| START_CR.matcher(f336b).find();
	}

	@Override
	public boolean isMusicalScores() {
		char ldr06 = getLeaderChar(underlayingMarc.getLeader().getTypeOfRecord());

		String f006 = underlayingMarc.getControlField("006");
		char f006_00 = (f006 != null) && (!f006.isEmpty()) ? Character.toLowerCase(f006.charAt(0)) : ' ';

		String f245h = underlayingMarc.getField("245", 'h');
		if (f245h == null) f245h = "";

		String f336b = underlayingMarc.getField("336", 'b');
		if (f336b == null) f336b = "";

		return MetadataUtils.containsChar(ARRAY_CD, ldr06)
				|| (MetadataUtils.containsChar(ARRAY_CD, f006_00) && HUDEBNINA.matcher(f245h).find())
				|| f336b.equalsIgnoreCase("tcm")
				|| f336b.equalsIgnoreCase("ntm")
				|| f336b.equalsIgnoreCase("ntv")
				|| f336b.equalsIgnoreCase("tcn");
	}

	@Override
	public boolean isVisualDocument() {
		char ldr06 = getLeaderChar(underlayingMarc.getLeader().getTypeOfRecord());

		String f006 = underlayingMarc.getControlField("006");
		char f006_00 = (f006 != null) && !f006.isEmpty() ? Character.toLowerCase(f006.charAt(0)) : ' ';

		String f007 = underlayingMarc.getControlField("007");
		char f007_00 = (f007 != null) && !f007.isEmpty() ? Character.toLowerCase(f007.charAt(0)) : ' ';

		String f245h = underlayingMarc.getField("245", 'h');
		if (f245h == null) f245h = "";

		String f300a = underlayingMarc.getField("300", 'a');
		if (f300a == null) f300a = "";

		String f336b = underlayingMarc.getField("336", 'b');
		if (f336b == null) f336b = "";

		String f337b = underlayingMarc.getField("337", 'b');
		if (f337b == null) f337b = "";

		String f338b = underlayingMarc.getField("338", 'b');
		if (f338b == null) f338b = "";

		return MetadataUtils.containsChar(ARRAY_KG, ldr06)
				|| MetadataUtils.containsChar(ARRAY_KG, f007_00)
				|| GRAFIKA.matcher(f245h).find()
				|| MetadataUtils.containsChar(ARRAY_KG, f006_00)
				|| f336b.equalsIgnoreCase("sti")
				|| f336b.equalsIgnoreCase("tci")
				|| f336b.equalsIgnoreCase("cri")
				|| f336b.equalsIgnoreCase("crt")
				|| f337b.equalsIgnoreCase("g")
				|| START_G.matcher(f338b).find()
				|| FOTOGRAFIE.matcher(f300a).find();
	}

	protected boolean isMicroform() {
		char ldr06 = getLeaderChar(underlayingMarc.getLeader().getTypeOfRecord());

		String f007 = underlayingMarc.getControlField("007");
		char f007_00 = (f007 != null) && !f007.isEmpty() ? Character.toLowerCase(f007.charAt(0)) : ' ';

		String f008 = underlayingMarc.getControlField("008");
		char f008_23 = (f008 != null) && (f008.length() > 23) ? Character.toLowerCase(f008.charAt(23)) : ' ';
		char f008_29 = (f008 != null) && (f008.length() > 29) ? Character.toLowerCase(f008.charAt(29)) : ' ';

		String f245h = underlayingMarc.getField("245", 'h');
		if (f245h == null) f245h = "";

		String f337b = underlayingMarc.getField("337", 'b');
		if (f337b == null) f337b = "";

		String f338b = underlayingMarc.getField("338", 'b');
		if (f338b == null) f338b = "";

		return (MetadataUtils.containsChar(ARRAY_ACDPT, ldr06) && MetadataUtils.containsChar(ARRAY_ABC, f008_23))
				|| (MetadataUtils.containsChar(ARRAY_EFK, ldr06) && f008_29 == 'b')
				|| f007_00 == 'h'
				|| MIKRODOKUMENT.matcher(f245h).find()
				|| f337b.equalsIgnoreCase("h")
				|| START_H.matcher(f338b).find();
	}

	@Override
	public boolean isBlindBraille() {
		String f007 = underlayingMarc.getControlField("007");
		char f007_00 = (f007 != null) && !f007.isEmpty() ? Character.toLowerCase(f007.charAt(0)) : ' ';
		char f007_01 = (f007 != null) && (f007.length() > 1) ? Character.toLowerCase(f007.charAt(1)) : ' ';

		String f245h = underlayingMarc.getField("245", 'h');
		if (f245h == null) f245h = "";

		String f336b = underlayingMarc.getField("336", 'b');
		if (f336b == null) f336b = "";

		return (f007_00 == 'f' && f007_01 == 'b' && HMATOVE_PISMO.matcher(f245h).find())
				|| (f007_00 == 't' && f007_01 == 'c')
				|| f336b.equalsIgnoreCase("tct")
				|| f336b.equalsIgnoreCase("tcm")
				|| f336b.equalsIgnoreCase("tci")
				|| f336b.equalsIgnoreCase("tcf");
	}

	protected boolean isElectronicSource() {
		char ldr06 = getLeaderChar(underlayingMarc.getLeader().getTypeOfRecord());

		String f006 = underlayingMarc.getControlField("006");
		char f006_00 = (f006 != null) && !f006.isEmpty() ? Character.toLowerCase(f006.charAt(0)) : ' ';
		char f006_06 = (f006 != null) && (f006.length() > 6) ? Character.toLowerCase(f006.charAt(6)) : ' ';

		String f008 = underlayingMarc.getControlField("008");
		char f008_23 = (f008 != null) && (f008.length() > 23) ? Character.toLowerCase(f008.charAt(23)) : ' ';
		char f008_29 = (f008 != null) && (f008.length() > 29) ? Character.toLowerCase(f008.charAt(29)) : ' ';

		String f338b = underlayingMarc.getField("338", 'b');
		if (f338b == null) f338b = "";

		return (MetadataUtils.containsChar(ARRAY_ACDIJPT, ldr06) && MetadataUtils.containsChar(ARRAY_OQ, f008_23))
				|| (MetadataUtils.containsChar(ARRAY_ACDIJPT, f006_00) && MetadataUtils.containsChar(ARRAY_OQ, f006_06))
				|| (MetadataUtils.containsChar(ARRAY_EFGKOPR, ldr06) && MetadataUtils.containsChar(ARRAY_OQ, f008_29))
				|| (MetadataUtils.containsChar(ARRAY_EFGKOPR, f006_00) && MetadataUtils.containsChar(ARRAY_OQ, f006_06))
				|| f338b.equalsIgnoreCase("cr");
	}

	protected boolean isComputerCarrier() {
		char ldr06 = getLeaderChar(underlayingMarc.getLeader().getTypeOfRecord());

		String f006 = underlayingMarc.getControlField("006");
		char f006_00 = (f006 != null) && !f006.isEmpty() ? Character.toLowerCase(f006.charAt(0)) : ' ';
		char f006_06 = (f006 != null) && (f006.length() > 6) ? Character.toLowerCase(f006.charAt(6)) : ' ';

		String f007 = underlayingMarc.getControlField("007");
		char f007_00 = (f007 != null) && !f007.isEmpty() ? Character.toLowerCase(f007.charAt(0)) : ' ';
		char f007_01 = (f007 != null) && (f007.length() > 1) ? Character.toLowerCase(f007.charAt(1)) : ' ';

		String f008 = underlayingMarc.getControlField("008");
		char f008_23 = (f008 != null) && (f008.length() > 23) ? Character.toLowerCase(f008.charAt(23)) : ' ';
		char f008_29 = (f008 != null) && (f008.length() > 29) ? Character.toLowerCase(f008.charAt(29)) : ' ';

		String f245h = underlayingMarc.getField("245", 'h');
		if (f245h == null) f245h = "";

		String f300a = underlayingMarc.getField("300", 'a');
		if (f300a == null) f300a = "";

		String f336b = underlayingMarc.getField("336", 'b');
		if (f336b == null) f336b = "";

		String f338b = underlayingMarc.getField("338", 'b');
		if (f338b == null) f338b = "";

		return ELEKTRONICKY_ZDROJ.matcher(f245h).find()
				|| (MetadataUtils.containsChar(ARRAY_ACDIJPT, ldr06) && f008_23 == 's')
				|| (MetadataUtils.containsChar(ARRAY_ACDIJPT, f006_00) && f006_06 == 's')
				|| (MetadataUtils.containsChar(ARRAY_EFGKOPR, ldr06) && f008_29 == 's')
				|| (MetadataUtils.containsChar(ARRAY_EFGKOPR, f006_00) && f006_06 == 's')
				|| (ldr06 == 'm' && f006_00 == 'm')
				|| (ldr06 == 'm' && MULTIMEDIUM.matcher(f245h).find() && CD_ROM.matcher(f300a).find())
				|| (f007_00 == 'c' && f007_01 != 'r')
				|| DISKETA.matcher(f300a).find()
				|| f336b.equalsIgnoreCase("cod")
				|| f336b.equalsIgnoreCase("cop")
				|| COMP_CARRIER_338B.matcher(f338b).matches();
	}

	private static final Pattern AUDIO_CD_AUDIODISC = Pattern.compile("audiodisk", Pattern.CASE_INSENSITIVE);

	protected HarvestedRecordFormatEnum getAudioFormat() {
		String f007 = underlayingMarc.getControlField("007");
		char f007_00 = (f007 != null) && !f007.isEmpty() ? Character.toLowerCase(f007.charAt(0)) : ' ';
		char f007_01 = (f007 != null) && (f007.length() > 1) ? Character.toLowerCase(f007.charAt(1)) : ' ';

		String f300 = underlayingMarc.getDataFields("300").toString();

		String f300a = underlayingMarc.getField("300", 'a');
		if (f300a == null) f300a = "";

		String f338b = underlayingMarc.getField("338", 'b');
		if (f338b == null) f338b = "";

		// AUDIO_CD
		if (isAudioCD()) return HarvestedRecordFormatEnum.AUDIO_CD;

		// AUDIO_LP
		if (GRAMOFONOVA_DESKA.matcher(f300).find()
				|| (ZVUKOVA_DESKA.matcher(f300).find() && ANALOG.matcher(f300).find())
				|| LP_OR_SP.matcher(f300a).find())
			return HarvestedRecordFormatEnum.AUDIO_LP;

		// AUDIO_CASSETTE
		if ((f007_00 == 's' && f338b.equalsIgnoreCase("ss"))
				|| (f007_00 == 's' && MetadataUtils.containsChar(ARRAY_ZGEIGT, f007_01))
				|| ZVUKOVA_KAZETA.matcher(f300).find()
				|| MC_OR_KZ_MGK.matcher(f300).find()
				|| MAGNETOFONOVA_KAZETA.matcher(f300).find())
			return HarvestedRecordFormatEnum.AUDIO_CASSETTE;

		// AUDIO_OTHER
		if (isAudioOther()) {
			if (AUDIO_CD_AUDIODISC.matcher(f300).find()) return HarvestedRecordFormatEnum.AUDIO_CD;
			else return HarvestedRecordFormatEnum.AUDIO_OTHER;
		}

		return null;
	}

	protected boolean isAudioCD() {
		String f300 = underlayingMarc.getDataFields("300").toString();
		String f500 = underlayingMarc.getDataFields("500").toString();
		String f300a = underlayingMarc.getField("300", 'a');
		if (f300a == null) f300a = "";

		for (String data : new String[]{f300, f500}) {
			if (KOMPAKTNI_DISK.matcher(data).find()
					|| (CD_R.matcher(data).find() && !CD_ROM.matcher(data).find()))
				return true;
		}
		return ZVUKOVE_CD.matcher(f300).find()
				|| (CD.matcher(f300a).find() && !CD_ROM.matcher(f300a).find())
				|| (ZVUKOVA_DESKA.matcher(f300).find() && DIGITAL_OR_12CM.matcher(f300).find());
	}

	private boolean isAudioOther() {
		char ldr06 = getLeaderChar(underlayingMarc.getLeader().getTypeOfRecord());

		String f006 = underlayingMarc.getControlField("006");
		char f006_00 = (f006 != null) && (!f006.isEmpty()) ? Character.toLowerCase(f006.charAt(0)) : ' ';

		String f007 = underlayingMarc.getControlField("007");
		char f007_00 = (f007 != null) && (!f007.isEmpty()) ? Character.toLowerCase(f007.charAt(0)) : ' ';

		String f245h = underlayingMarc.getField("245", 'h');
		if (f245h == null) f245h = "";

		String f336b = underlayingMarc.getField("336", 'b');
		if (f336b == null) f336b = "";

		String f337b = underlayingMarc.getField("337", 'b');
		if (f337b == null) f337b = "";

		String f338b = underlayingMarc.getField("338", 'b');
		if (f338b == null) f338b = "";

		return (ldr06 == 'i' || ldr06 == 'j')
				|| f007_00 == 's'
				|| ZVUKOVY_ZAZNAM.matcher(f245h).find()
				|| f337b.equalsIgnoreCase("s")
				|| (f006_00 == 'i' || f006_00 == 'j')
				|| START_S.matcher(f338b).find()
				|| f007_00 == 'i'
				|| (f336b.equalsIgnoreCase("spw") || f336b.equalsIgnoreCase("snd"));
	}

	protected boolean isAudioDVD() {
		char ldr06 = getLeaderChar(underlayingMarc.getLeader().getTypeOfRecord());

		String f300a = underlayingMarc.getField("300", 'a');
		if (f300a == null) f300a = "";

		// DVD
		return (ldr06 == 'i' || ldr06 == 'j') && DVD.matcher(f300a).find();
	}

	protected HarvestedRecordFormatEnum getVideoDocument() {
		char ldr06 = getLeaderChar(underlayingMarc.getLeader().getTypeOfRecord());

		String f006 = underlayingMarc.getControlField("006");
		char f006_00 = (f006 != null) && !f006.isEmpty() ? Character.toLowerCase(f006.charAt(0)) : ' ';
		char f006_16 = (f006 != null) && (f006.length() > 16) ? Character.toLowerCase(f006.charAt(16)) : ' ';

		String f007 = underlayingMarc.getControlField("007");
		char f007_00 = (f007 != null) && !f007.isEmpty() ? Character.toLowerCase(f007.charAt(0)) : ' ';
		char f007_04 = (f007 != null) && (f007.length() > 4) ? Character.toLowerCase(f007.charAt(4)) : ' ';

		String f008 = underlayingMarc.getControlField("008");
		char f008_33 = (f008 != null) && (f008.length() > 33) ? Character.toLowerCase(f008.charAt(33)) : ' ';

		String f245h = underlayingMarc.getField("245", 'h');
		if (f245h == null) f245h = "";

		String f300 = underlayingMarc.getDataFields("300").toString();

		String f300a = underlayingMarc.getField("300", 'a');
		if (f300a == null) f300a = "";

		String f336b = underlayingMarc.getField("336", 'b');
		if (f336b == null) f336b = "";

		String f337b = underlayingMarc.getField("337", 'b');
		if (f337b == null) f337b = "";

		String f338b = underlayingMarc.getField("338", 'b');
		if (f338b == null) f338b = "";

		String f500 = underlayingMarc.getDataFields("500").toString();

		// Bluray
		if (ldr06 == 'g' && BLURAY.matcher(f300).find()) return HarvestedRecordFormatEnum.VIDEO_BLURAY;

		// VHS
		if (VHS.matcher(f300).find()
				|| (f007_00 == 'v' && f007_04 == 'b')
				|| VIDEOKAZETA.matcher(f300a).find())
			return HarvestedRecordFormatEnum.VIDEO_VHS;

		// DVD
		if ((f007_00 == 'v' && f007_04 == 'v')
				|| DVD_VIDEO.matcher(f300a).find()
				|| VIDEODISK.matcher(f300).find()
				|| VIDEODISK.matcher(f500).find()
				|| f338b.equalsIgnoreCase("vd"))
			return HarvestedRecordFormatEnum.VIDEO_DVD;

		// CD
		if (ldr06 == 'g' && CD.matcher(f300a).find()) return HarvestedRecordFormatEnum.VIDEO_CD;

		// others
		if (MetadataUtils.containsChar(ARRAY_VM, f007_00)
				|| VIDEOZAZNAM.matcher(f245h).find()
				|| f337b.equalsIgnoreCase("v")
				|| (ldr06 == 'g' && MetadataUtils.containsChar(ARRAY_VM, f008_33))
				|| (f006_00 == 'g' && MetadataUtils.containsChar(ARRAY_VM, f006_16))
				|| START_V.matcher(f338b).find()
				|| (f336b.equalsIgnoreCase("tdi") || f336b.equalsIgnoreCase("tdm"))
				|| VIDEO_OTHER_F338.matcher(f338b).matches())
			return HarvestedRecordFormatEnum.VIDEO_OTHER;

		return null;
	}

	protected boolean isVideoDVD() {
		String f300a = underlayingMarc.getField("300", 'a');
		if (f300a == null) f300a = "";

		// VIDEO_DVD
		return DVD.matcher(f300a).find();
	}

	protected boolean isMacan() {
		for (String tag : new String[]{"260", "264"}) {
			for (String f260b : underlayingMarc.getFields(tag, 'b')) {
				if (MACAN.matcher(f260b).find() || KTN.matcher(f260b).find()) {
					return true;
				}
			}
		}
		return false;
	}

	protected boolean isOthers() {
		char ldr06 = getLeaderChar(underlayingMarc.getLeader().getTypeOfRecord());

		String f006 = underlayingMarc.getControlField("006");
		char f006_00 = (f006 != null) && !f006.isEmpty() ? Character.toLowerCase(f006.charAt(0)) : ' ';

		String f007 = underlayingMarc.getControlField("007");
		char f007_00 = (f007 != null) && !f007.isEmpty() ? Character.toLowerCase(f007.charAt(0)) : ' ';

		String f008 = underlayingMarc.getControlField("008");
		char f008_33 = (f008 != null) && (f008.length() > 33) ? Character.toLowerCase(f008.charAt(33)) : ' ';

		String f336b = underlayingMarc.getField("336", 'b');
		if (f336b == null) f336b = "";

		String f337b = underlayingMarc.getField("337", 'b');
		if (f337b == null) f337b = "";

		String f338b = underlayingMarc.getField("338", 'b');
		if (f338b == null) f338b = "";

		return MetadataUtils.containsChar(ARRAY_OPR, ldr06)
				|| (f006_00 == 'o' && f007_00 == 'o')
				|| OTHER_F336.matcher(f336b).matches()
				|| f008_33 == 'd'
				|| f006_00 == 'r'
				|| (f007_00 == 'z' && f336b.equalsIgnoreCase("zzz"))
				|| (f337b.equalsIgnoreCase("x") || f337b.equalsIgnoreCase("z"))
				|| f338b.equalsIgnoreCase("zu");
	}

	protected char getLeaderChar(final char c) {
		return Character.toLowerCase(c);
	}

	@Override
	public List<HarvestedRecordFormatEnum> getDetectedFormatList() {
		List<HarvestedRecordFormatEnum> hrf = new ArrayList<>();

		if (isBook()) {
			if (isMacan()) {
				hrf.add(HarvestedRecordFormatEnum.BLIND_BRAILLE);
				return hrf;
			}
			hrf.add(HarvestedRecordFormatEnum.BOOKS);
		}
		if (isPeriodical()) hrf.add(HarvestedRecordFormatEnum.PERIODICALS);
		if (isArticle()) hrf.add(HarvestedRecordFormatEnum.ARTICLES);
		if (isArticle773()) return Collections.singletonList(HarvestedRecordFormatEnum.ARTICLES);
		if (isMap()) hrf.add(HarvestedRecordFormatEnum.MAPS);
		if (isMusicalScores()) hrf.add(HarvestedRecordFormatEnum.MUSICAL_SCORES);
		if (isVisualDocument()) hrf.add(HarvestedRecordFormatEnum.VISUAL_DOCUMENTS);
		if (isMicroform()) hrf.add(HarvestedRecordFormatEnum.OTHER_MICROFORMS);
		if (isBlindBraille()) hrf.add(HarvestedRecordFormatEnum.BLIND_BRAILLE);
		HarvestedRecordFormatEnum audio = getAudioFormat();
		if (audio != null) {
			if (isAudioDVD()) hrf.add(HarvestedRecordFormatEnum.AUDIO_DVD);
			else hrf.add(audio);
			if (isMacan()) {
				hrf.clear();
				hrf.add(HarvestedRecordFormatEnum.BLIND_AUDIO);
				return hrf;
			}
		}
		HarvestedRecordFormatEnum video = getVideoDocument();
		if (video != null) {
			if (isVideoDVD()) {
				if (!hrf.contains(HarvestedRecordFormatEnum.VIDEO_DVD)) hrf.add(HarvestedRecordFormatEnum.VIDEO_DVD);
			} else hrf.add(video);
		}
		if (isComputerCarrier()) hrf.add(HarvestedRecordFormatEnum.OTHER_COMPUTER_CARRIER);
		if (isOthers()) hrf.add(HarvestedRecordFormatEnum.OTHER_OTHER);
		if (hrf.isEmpty()) hrf.add(HarvestedRecordFormatEnum.OTHER_OTHER);

		if (hrf.size() > 1 && hrf.contains(HarvestedRecordFormatEnum.BOOKS)) {
			hrf.remove(HarvestedRecordFormatEnum.BOOKS);
		}

		return hrf;
	}

	@Override
	public Long getScale() {
		String scaleStr = underlayingMarc.getField("255", 'a');
		if (scaleStr == null) {
			return null;
		}
		Matcher matcher = SCALE_PATTERN.matcher(scaleStr);
		if (matcher.find()) {
			String strValue = CleaningUtils.replaceAll(matcher.group(0), SCALE_REPLACE, "");
			try {
				return Long.valueOf(strValue);
			} catch (NumberFormatException nfe) {
				return null;
			}
		}
		return null;
	}

	@Override
	public String getUUId() {
		String baseStr = underlayingMarc.getField("856", 'u');
		if (baseStr == null) {
			return null;
		}
		Matcher matcher = UUID_PATTERN.matcher(baseStr);
		if (matcher.find()) {
			String uuidStr = matcher.group(0);
			if (uuidStr != null && uuidStr.length() > 5) {
				return uuidStr.substring(5);
			}
		}
		return null;
	}

	private static final String[] FIELDS1XX = {"100", "110", "111", "130"};
	private static final String[] FIELDS6XX = {"600", "610", "611", "630", "648", "650", "651", "653", "654",
			"655", "656", "657", "658", "662", "690", "691", "692", "693", "694", "695", "696", "697", "698", "699"};
	private static final String[] FIELDS7XX = {"700", "710", "711", "720", "730", "740", "751", "752", "753",
			"754", "760", "762", "765", "767", "770", "772", "773", "774", "775", "776", "777", "780", "785", "786", "787"};
	private static final Pattern WEIGHT_2_TO_4 = Pattern.compile("[2-4]");
	private static final Pattern WEIGHT_05_TO_9 = Pattern.compile("[05-9]");
	private static final Pattern WEIGHT_RDA = Pattern.compile("rda", Pattern.CASE_INSENSITIVE);

	@Override
	public Long getWeight(Long baseWeight) {
		Long weight = 0L;
		if (baseWeight != null) weight = baseWeight;

		if (underlayingMarc.getDataFields("245").isEmpty()) {
			return 0L;
		}

		String ldr17 = Character.toString(underlayingMarc.getLeader().getImplDefined2()[0]);
		if (ldr17.equals("1")) weight -= 1;
		else if (WEIGHT_2_TO_4.matcher(ldr17).matches()) weight -= 2;
		else if (WEIGHT_05_TO_9.matcher(ldr17).matches()) weight -= 3;
		if (underlayingMarc.getControlField("008") == null) weight -= 1;
		if (underlayingMarc.getDataFields("300").isEmpty()) weight -= 1;

		boolean exists1xx = false;
		for (String key : FIELDS1XX) {
			if (!underlayingMarc.getDataFields(key).isEmpty()) {
				exists1xx = true;
				break;
			}
		}
		boolean f245Ind1 = false;
		for (DataField dataField : underlayingMarc.getDataFields("245")) {
			if (dataField.getIndicator1() == 0) f245Ind1 = true;
		}

		if (!exists1xx && !f245Ind1) {
			weight -= 1;
		}

		if (!underlayingMarc.getDataFields("080").isEmpty() || !underlayingMarc.getDataFields("072").isEmpty()) {
			weight += 1;
		}
		if (!underlayingMarc.getDataFields("964").isEmpty()) weight += 1;
		else {
			for (String key : FIELDS6XX) {
				if (!underlayingMarc.getDataFields(key).isEmpty()) {
					weight += 1;
					break;
				}
			}
		}

		if (!getISBNs().isEmpty() || !getISSNs().isEmpty() || !getCNBs().isEmpty()) {
			weight += 1;
		}

		boolean exist7in1xx = false;
		for (String key : FIELDS1XX) {
			if (underlayingMarc.getField(key, '7') != null) {
				weight += 1;
				exist7in1xx = true;
				break;
			}
		}
		if (!exist7in1xx) {
			for (String key : FIELDS7XX) {
				if (underlayingMarc.getField(key, '7') != null) {
					weight += 1;
					break;
				}
			}
		}

		for (String subfield : underlayingMarc.getFields("040", 'e')) {
			if (WEIGHT_RDA.matcher(subfield).matches()) {
				weight += 1;
				break;
			}
		}

		return weight;
	}

	@Override
	public String getAuthorAuthKey() {
		String f100s7 = underlayingMarc.getField("100", '7');
		if (f100s7 != null) {
			return f100s7;
		}
		String f700s7 = underlayingMarc.getField("700", '7');
		if (f700s7 != null) {
			return f700s7;
		}
		return null;

	}

	@Override
	public String getAuthorString() {
		String f100a = underlayingMarc.getField("100", 'a');
		if (f100a != null) {
			return f100a;
		}
		String f700a = underlayingMarc.getField("700", 'a');
		if (f700a != null) {
			return f700a;
		}
		return null;
	}

	@Override
	public String getClusterId() {
		// implemented only in selected specialization
		return null;
	}

	@Override
	public List<Oclc> getOclcs() {
		List<Oclc> result = new ArrayList<>();
		if (getLanguages().contains("cze")) return result;
		for (DataField df : underlayingMarc.getDataFields("035")) {
			Subfield subA = df.getSubfield('a');
			if (subA == null) {
				continue;
			}
			Matcher matcher = OCLC_PATTERN.matcher(subA.getData());
			if (matcher.matches() && matcher.groupCount() >= 2) {
				Oclc oclc = new Oclc();
				oclc.setOclcStr(matcher.group(2));
				result.add(oclc);
			}
		}
		return result;
	}

	@Override
	public List<String> getLanguages() {
		Set<String> result = new HashSet<>();
		for (DataField df : underlayingMarc.getDataFields("041")) {
			for (Subfield subA : df.getSubfields('a')) {
				String lang;
				if (subA.getData().toLowerCase().equals("cze")) {
					lang = "cze";
				} else if (subA.getData().toLowerCase().equals("eng")) {
					lang = "eng";
				} else {
					lang = "oth";
				}
				result.add(lang);
			}
		}
		if (result.isEmpty()) {
			String cf = underlayingMarc.getControlField("008");
			if (cf != null && cf.length() > 39) {
				String substr = cf.substring(35, 38);
				String lang = null;
				if (substr.toLowerCase().equals("cze")) {
					lang = "cze";
				} else if (substr.toLowerCase().equals("eng")) {
					lang = "eng";
				}
				if (lang != null) {
					result.add(lang);
				}
			}
		}
		return new ArrayList<>(result);
	}

	@Override
	public boolean matchFilter() {
		if (underlayingMarc.getDataFields("245").isEmpty()) return false;
		for (DataField df : underlayingMarc.getDataFields("914")) {
			if (df.getSubfield('a') != null
					&& CPK0_PATTERN.matcher(df.getSubfield('a').getData()).matches()) return false;
		}
		// more rules in institution specific classes
		return true;
	}

	@Override
	public String getRaw001Id() {
		return underlayingMarc.getControlField("001");
	}

	private static final Pattern CITATION_OR_AT = Pattern.compile("[at]");
	private static final Pattern CITATION_OR_CDM = Pattern.compile("[cdm]");
	private static final Pattern CITATION_OR_IS = Pattern.compile("[is]");
	private static final Pattern CITATION_CONTRIBUTION =
			Pattern.compile(".*sborník.*|.*proceedings.*|.*almanach.*", Pattern.CASE_INSENSITIVE);
	private static final Pattern CITATION_OR_AB = Pattern.compile("[ab]");
	private static final Pattern CITATION_OR_EF = Pattern.compile("[ef]");
	private static final Pattern CITATION_OR_CDKGIJOPR = Pattern.compile("[cdkgijopr]");

	@Override
	public CitationRecordType getCitationFormat() {
		String ldr06 = Character.toString(underlayingMarc.getLeader().getTypeOfRecord()).toLowerCase();
		String ldr07 = Character.toString(underlayingMarc.getLeader().getImplDefined1()[0]).toLowerCase();

		Boolean exists85641 = false; // url, for electronic version
		for (DataField df : underlayingMarc.getDataFields("856")) {
			if (df.getIndicator1() == '4' && df.getIndicator2() == '1') {
				exists85641 = true;
			}
		}

		if (!underlayingMarc.getDataFields("502").isEmpty()) {
			return CitationRecordType.ACADEMIC_WORK;
		}

		if (CITATION_OR_AT.matcher(ldr06).matches() && CITATION_OR_CDM.matcher(ldr07).matches()) {
			if (exists85641) return CitationRecordType.ELECTRONIC_BOOK;
			else return CitationRecordType.BOOK;
		}

		if (CITATION_OR_IS.matcher(ldr07).matches()) {
			if (exists85641) return CitationRecordType.ELECTRONIC_PERIODICAL;
			else return CitationRecordType.PERIODICAL;
		}

		for (DataField df : underlayingMarc.getDataFields("773")) {
			if (CITATION_CONTRIBUTION.matcher(df.toString()).matches()) {
				if (exists85641) return CitationRecordType.ELECTRONIC_CONTRIBUTION_PROCEEDINGS;
				else return CitationRecordType.CONTRIBUTION_PROCEEDINGS;
			}
		}

		if (CITATION_OR_AB.matcher(ldr07).matches()) {
			if (exists85641) return CitationRecordType.ELECTRONIC_ARTICLE;
			else return CitationRecordType.ARTICLE;
		}

		if (CITATION_OR_EF.matcher(ldr06).matches()) return CitationRecordType.MAPS;

		if (CITATION_OR_CDKGIJOPR.matcher(ldr06).matches()) return CitationRecordType.OTHERS;

		return CitationRecordType.ERROR;
	}

	@Override
	public List<String> getBarcodes() {
		return underlayingMarc.getFields("996", 'b');
	}

	@Override
	public List<Ismn> getISMNs() {
		List<Ismn> results = new ArrayList<>();
		Long ismnCounter = 0L;
		Ismn ismn;

		for (DataField df : underlayingMarc.getDataFields("024")) {
			try {
				ismn = ISMNUtils.createIsmn(df);
			} catch (NoDataException nde) {
				continue;
			} catch (NumberFormatException nfe) {
				logger.info(String.format("Invalid ISMN: %s", nfe.getMessage()));
				continue;
			}
			ismn.setOrderInRecord(++ismnCounter);
			results.add(ismn);
		}
		return results;
	}

	@Override
	public String getAuthorityId() {
		// implemented only in institution specific classes
		return null;
	}

	@Override
	public List<String> getUrls() {
		return getUrls(Constants.DOCUMENT_AVAILABILITY_UNKNOWN);
	}

	protected List<String> getUrls(String availability) {
		List<String> result = new ArrayList<>();

		for (DataField df : underlayingMarc.getDataFields("856")) {
			if (df.getSubfield('u') == null) {
				continue;
			}
			String link = df.getSubfield('u').getData();
			String comment = "";
			String sub3 = null, subY = null, subZ = null;

			if (df.getSubfield('3') != null) {
				sub3 = df.getSubfield('3').getData();
			}
			if (df.getSubfield('y') != null) {
				subY = df.getSubfield('y').getData();
			}
			if (df.getSubfield('z') != null) {
				subZ = df.getSubfield('z').getData();
			}

			if (sub3 != null) {
				comment = (subZ != null) ? String.format(URL_COMMENT_FORMAT, sub3, subZ) : sub3;
			} else if (subY != null) {
				comment = (subZ != null) ? String.format(URL_COMMENT_FORMAT, subY, subZ) : subY;
			} else if (subZ != null) {
				comment = subZ;
			}
			result.add(MetadataUtils.generateUrl(availability, link, comment));
		}
		return result;
	}

	@Override
	public String getPolicyKramerius() {
		// Nothing to return
		return "unknown";
	}

	@Override
	public String filterSubjectFacet() {
		// implemented only in institution specific classes
		return null;
	}

	@Override
	public List<Ean> getEANs() {
		List<Ean> results = new ArrayList<>();
		Long eanCounter = 0L;
		Ean ean;

		for (DataField df : underlayingMarc.getDataFields("024")) {
			try {
				ean = EANUtils.createEan(df);
			} catch (NoDataException nde) {
				continue;
			} catch (NumberFormatException nfe) {
				logger.info(String.format("Invalid EAN: %s", nfe.getMessage()));
				continue;
			}
			ean.setOrderInRecord(++eanCounter);
			results.add(ean);
		}
		return results;
	}

	/**
	 * get {@link ShortTitle} of record
	 *
	 * @return all 245anp and 240anp, if not contains subfield 'b'
	 */
	@Override
	public List<ShortTitle> getShortTitles() {
		List<ShortTitle> results = new ArrayList<>();
		Long shortTitleCounter = 0L;
		for (String tag : TITLE_TAGS) {
			for (DataField df : underlayingMarc.getDataFields(tag)) {
				if (df.getSubfield('b') == null) continue;
				String titleText = parseTitleValue(df, SHORT_TITLE_SUBFIELDS);
				if (!titleText.isEmpty()) {
					results.add(ShortTitle.create(titleText, ++shortTitleCounter,
							MetadataUtils.similarityEnabled(df, titleText)));
				}
			}
		}
		return results;
	}

	/**
	 * join subfields data for {@link Title} or {@link ShortTitle}
	 *
	 * @param DF        {@link DataField}
	 * @param SUBFIELDS result subfields, TITLE - abnp, SHORT_TITLE - anp
	 * @return String
	 */
	private String parseTitleValue(final DataField DF, final char[] SUBFIELDS) {
		StringBuilder builder = new StringBuilder();
		for (Subfield subfield : DF.getSubfields()) {
			if (MetadataUtils.hasTrailingPunctuation(builder.toString())) {
				builder.append(' ');
			}
			if (Chars.contains(SUBFIELDS, subfield.getCode())) {
				builder.append(subfield.getData());
			}
		}
		return builder.toString().trim();
	}

	@Override
	public List<String> getDefaultStatuses() {
		// implemented in institution specific classes
		return Collections.emptyList();
	}

	@Override
	public List<String> getInternationalPatentClassfication() {
		// implemented in institution specific classes
		return null;
	}

	@Override
	public TezaurusKey getTezaurusKey() {
		// implemented in institution specific classes
		return null;
	}

	@Override
	public Boolean getMetaproxyBool() {
		Set<String> fieldTags = underlayingMarc.getAllFields().keySet();
		if (underlayingMarc.getControlField("008") == null) return false;
		if (!fieldTags.contains("300") || !fieldTags.contains("245")
				|| (!fieldTags.contains("260") && !fieldTags.contains("264"))) return false;
		boolean f245ind1 = false;
		for (DataField df : underlayingMarc.getDataFields("245")) {
			if (df.getIndicator1() == '0') f245ind1 = true;
		}
		if (!f245ind1) {
			boolean f1xxOr7xx = false;
			for (String tag : fieldTags) {
				if (METAPROXY_TAG_PATTERN.matcher(tag).matches()) {
					f1xxOr7xx = true;
					break;
				}
			}
			if (!f1xxOr7xx) return false;
		}
		return true;
	}

	@Override
	public List<PublisherNumber> getPublisherNumber() {
		List<PublisherNumber> results = new ArrayList<>();
		Long i = 0L;
		for (DataField df : underlayingMarc.getDataFields("028")) {
			if (df.getIndicator1() == '0' && df.getSubfield('a') != null) {
				String result = PUBLISHER_NUMBER_PATTERN.matcher(df.getSubfield('a').getData().toLowerCase()).replaceAll("");
				results.add(new PublisherNumber(result, ++i));
			}
		}
		return results;
	}

	@Override
	public String getSourceInfoX() {
		return underlayingMarc.getField("773", 'x');
	}

	@Override
	public String getSourceInfoT() {
		return underlayingMarc.getField("773", 't');
	}

	@Override
	public String getSourceInfoG() {
		return underlayingMarc.getField("773", 'g');
	}

	protected String generateSfxUrl(String url, String id, Map<String, String> specificParams) {
		Map<String, String> allParams = new HashMap<>();
		allParams.put("url_ver", "Z39.88-2004");
		allParams.put("sfx.ignore_date_threshold", "1");
		allParams.put("rft.object_id", id);

		allParams.putAll(specificParams);
		return UrlUtils.buildUrl(url, allParams);
	}

	protected String getSfxInstitute() {
		return getSfxInstitute(Collections.emptyMap());
	}

	protected String getSfxInstitute(Map<String, String> sfx_map) {
		String prefix = underlayingMarc.getControlField("003");
		prefix = prefix.toUpperCase();
		if (sfx_map.containsKey(prefix)) {
			prefix = sfx_map.get(prefix);
		}
		return prefix;
	}

	@Override
	public List<Authority> getAllAuthorAuthKey() {
		Set<String> ids = Arrays.stream(AUTHORITY_ID_TAGS)
				.flatMap(tag -> underlayingMarc.getFields(tag, '7').stream()).collect(Collectors.toSet());
		return ids.stream().map(Authority::create).collect(Collectors.toList());
	}

	@Override
	public List<String> getConspectusForView() {
		List<String> subCat = underlayingMarc.getFields("072", 'a');
		List<String> cat = underlayingMarc.getFields("072", '9');
		List<String> results = new ArrayList<>();
		results.addAll(subCat.stream().map(name -> "subcat " + name).collect(Collectors.toList()));
		results.addAll(cat.stream().map(name -> "cat " + name).collect(Collectors.toList()));
		return results;
	}

	/**
	 * first of 260b, 264b, books only
	 *
	 * @return String
	 */
	@Override
	public String getPublisher() {
		if (!getDetectedFormatList().contains(HarvestedRecordFormatEnum.BOOKS)) return null;
		String publisher = underlayingMarc.getField("260", 'b');
		if (publisher != null) return publisher;
		return underlayingMarc.getField("264", 'b');
	}

	/**
	 * first number from 250a, books only
	 *
	 * @return String
	 */
	@Override
	public String getEdition() {
		if (!getDetectedFormatList().contains(HarvestedRecordFormatEnum.BOOKS)) return null;
		String data = underlayingMarc.getField("250", 'a');
		if (data == null) return null;
		Matcher matcher = NUMBER_PATTERN.matcher(data);
		if (matcher.find()) return matcher.group(0);
		return null;
	}

	/**
	 * 245anp, books only
	 * numbers less than 10 converted to roman numerals
	 *
	 * @return Set of {@link AnpTitle}
	 */
	@Override
	public Set<AnpTitle> getAnpTitle() {
		if (!getDetectedFormatList().contains(HarvestedRecordFormatEnum.BOOKS)) return Collections.emptySet();
		Set<AnpTitle> results = new HashSet<>();
		for (DataField df : underlayingMarc.getDataFields("245")) {
			String titleText = parseTitleValue(df, SHORT_TITLE_SUBFIELDS);
			if (!titleText.isEmpty()) {
				boolean similarity = MetadataUtils.similarityEnabled(df, titleText);
				titleText = RomanNumeralsUtils.getRomanNumerals(titleText);
				for (Pattern pattern : ANP_TITLE_REMOVE_WORDS_PATTERNS) {
					titleText = CleaningUtils.replaceAll(titleText, pattern, "");
				}
				results.add(AnpTitle.create(titleText, similarity));
			}
		}
		return results;
	}
}
