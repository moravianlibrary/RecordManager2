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
import org.apache.commons.lang3.tuple.Pair;
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

import static cz.mzk.recordmanager.server.model.HarvestedRecordFormat.HarvestedRecordFormatEnum.*;

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
	private static final Pattern CNB_PATTERN = Pattern.compile("cnb[0-9]+");
	private static final Pattern FIELD130A = Pattern.compile("(.*)\\([^)]*\\)$");
	private static final Pattern Z_CYKLU = Pattern.compile("z cyklu:", Pattern.CASE_INSENSITIVE);
	private static final Pattern BOOKPORT = Pattern.compile("\\.bookport\\.cz", Pattern.CASE_INSENSITIVE);
	protected static final Pattern EBOOKS_URL = Pattern.compile("\\.bookport\\.cz|\\.palmknihy\\.cz", Pattern.CASE_INSENSITIVE);
	public static final Pattern PALMKNIHY_ID = Pattern.compile("https://www.palmknihy.cz/kniha/(\\d*).*", Pattern.CASE_INSENSITIVE);

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
	private static final Pattern CM30 = Pattern.compile("30\\s*cm", Pattern.CASE_INSENSITIVE);
	private static final Pattern GRAMOFONOVA_DESKA = Pattern.compile("gramofonov(?:[aáeé]|ych|ých)\\sdes(?:ka|ky|ek)", Pattern.CASE_INSENSITIVE);
	private static final Pattern ANALOG = Pattern.compile("analog", Pattern.CASE_INSENSITIVE);
	private static final Pattern LP_OR_SP = Pattern.compile("LP|SP");
	private static final Pattern ZVUKOVA_KAZETA = Pattern.compile("(?:zvukov(?:a|á|e|é|ych|ých)\\s+|audio)kaze(?:ta|ty|t)", Pattern.CASE_INSENSITIVE);
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
	private static final Pattern ZVUKOVY_DISK = Pattern.compile("zvukov(?:ý|é|ých)\\sdisk[ůy]?", Pattern.CASE_INSENSITIVE);
	private static final Pattern BACHELOR = Pattern.compile("^bakal[aá][rř]sk[aáeé]", Pattern.CASE_INSENSITIVE);
	private static final Pattern MASTER = Pattern.compile("^diplomov[aáeé]", Pattern.CASE_INSENSITIVE);
	private static final Pattern ADVANCED_MASTER = Pattern.compile("^rigor[oó]zn[ií]", Pattern.CASE_INSENSITIVE);
	private static final Pattern DISSERTATION = Pattern.compile("^(?:(?:di[sz]{1,2}ertace|dissertation|di[sz]erta[cč]n[ií]|di[s]{1,2}|kandid[aá]tsk[aá]|doktorsk[aá])\\b)|(?:kand\\.|dokt\\.|doktor\\.|doktorand\\.)", Pattern.CASE_INSENSITIVE);
	private static final Pattern HABILITATION = Pattern.compile("^habilita(?:ce|[cč]n[ií])", Pattern.CASE_INSENSITIVE);
	private static final Pattern BOARD_GAMES = Pattern.compile("(?:deskov[eé]|karetn[ií]|spole[cč]ensk[eé]|stoln[ií])\\shry", Pattern.CASE_INSENSITIVE);

	private static final Long MAX_PAGES = 10_000_000L;
	private static final String INVALID_YEAR = "Invalid year: %s";
	private static final String[] TITLE_TAGS = {"245"};
	private static final String[] AUTHORITY_ID_TAGS = {"100", "110", "111", "700", "710", "711"};
	private static final char[] SHORT_TITLE_SUBFIELDS = {'a', 'n', 'p'};
	private static final char[] TITLE_SUBFIELDS = {'a', 'b', 'n', 'p'};
	private static final List<String> ZISKEJ_ABSENT_996 = Collections.singletonList("A");

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
	private static final List<String> ENTITY_RELATIONSHIP =
			Arrays.asList("aut", "edt", "cmp", "ivr", "ive", "org", "drt", "ant", "ctb", "ccp");
	private static final List<String> BL_AUTHOR_RELATIONSHIP = Arrays.asList("ccp", "ant", "aut");
	private static final List<String> BL_TOPIC_KEY_STOP_WORDS_650 = Arrays.asList("d006801", "ph115615", "ph128175", "ph114390",
			"ph116858", "ph116861", "d005260", "ph114056", "d005260", "ph135292");
	private static final List<HarvestedRecordFormatEnum> BL_AUTHOR_VIDEO =
			Arrays.asList(HarvestedRecordFormatEnum.VIDEO_BLURAY,
					HarvestedRecordFormatEnum.VIDEO_CD,
					HarvestedRecordFormatEnum.VIDEO_DOCUMENTS,
					HarvestedRecordFormatEnum.VIDEO_DVD,
					HarvestedRecordFormatEnum.VIDEO_OTHER,
					HarvestedRecordFormatEnum.VIDEO_VHS);

	private static final int EFFECTIVE_LENGTH_PUBLISHER_NUMBER = 255;

	private static final String URL_COMMENT_FORMAT = "%s (%s)";
	private static final List<String> TITLE_REMOVE_WORDS = new BufferedReader(new InputStreamReader(
			new ClasspathResourceProvider().getResource("/list/title_remove_words.txt"), StandardCharsets.UTF_8))
			.lines().collect(Collectors.toCollection(ArrayList::new));

	private static final List<Pattern> TITLE_REMOVE_WORDS_PATTERNS = TITLE_REMOVE_WORDS.stream()
			.map(w -> Pattern.compile("\\b" + w + "\\b", Pattern.CASE_INSENSITIVE)).collect(Collectors.toList());

	private static final List<Pair<Pattern, String>> SOURCE_INFO_G_PATTERNS =
			FileUtils.openFile("/list/source_info_g_patterns.txt").stream().map(line -> {
				String[] split = line.split(" = ");
				return Pair.of(Pattern.compile("\\b" + split[0] + "\\b", Pattern.CASE_INSENSITIVE), split[1]);
			}).collect(Collectors.toList());

	private static final List<Pattern> DEDUP_STOP_WORDS = new BufferedReader(new InputStreamReader(
			new ClasspathResourceProvider().getResource("/stopwords/dedup.txt"), StandardCharsets.UTF_8))
			.lines().map(s -> Pattern.compile("\\b" + s + "\\b", Pattern.CASE_INSENSITIVE)).collect(Collectors.toList());

	private static final List<HarvestedRecordFormatEnum> ZISKEJ_FORMAT_ALLOWED = new ArrayList<>();

	static {
		ZISKEJ_FORMAT_ALLOWED.add(BOOKS);
	}

	protected static final List<HarvestedRecordFormatEnum> EDD_FORMAT_ALLOWED = new ArrayList<>();

	static {
		EDD_FORMAT_ALLOWED.add(BOOKS);
		EDD_FORMAT_ALLOWED.add(HarvestedRecordFormatEnum.PERIODICALS);
		EDD_FORMAT_ALLOWED.add(HarvestedRecordFormatEnum.ARTICLES);
	}

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
				logger.debug(String.format("Invalid ISSN: %s", nfe.getMessage()));
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

		Matcher matcher;
		for (DataField field : underlayingMarc.getDataFields("015")) {
			for (Subfield subfieldA : field.getSubfields('a')) {
				if (subfieldA != null && (matcher = CNB_PATTERN.matcher(subfieldA.getData())).find()) {
					cnbs.add(Cnb.create(matcher.group(0)));
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
				logger.debug(String.format(INVALID_YEAR, matcher.group(0)));
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
				logger.debug(String.format("Invalid ISBN: %s", nfe.getMessage()));
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
			logger.debug(String.format(INVALID_YEAR, matcher.group(0)));
		}
		return null;
	}

	/**
	 * get {@link Title} of record
	 *
	 * @return all 245abnp
	 */
	@Override
	public List<Title> getTitle() {
		List<Title> result = new ArrayList<>();
		Long titleOrder = 0L;
		for (String key : TITLE_TAGS) {
			for (DataField df : underlayingMarc.getDataFields(key)) {
				String titleText = parseTitleValue(df, TITLE_SUBFIELDS);
				if (!titleText.isEmpty()) {
					boolean similarity = MetadataUtils.similarityEnabled(df, titleText);
					titleText = RomanNumeralsUtils.getRomanNumerals(titleText);
					for (Pattern pattern : TITLE_REMOVE_WORDS_PATTERNS) {
						titleText = CleaningUtils.replaceAll(titleText, pattern, "");
					}
					result.add(Title.create(titleText, ++titleOrder, similarity));
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

		String f245h = underlayingMarc.getField("245", 'h');
		if (f245h == null) f245h = "";

		String f336b = underlayingMarc.getField("336", 'b');
		if (f336b == null) f336b = "";

		return MetadataUtils.containsChar(ARRAY_CD, ldr06)
				|| HUDEBNINA.matcher(f245h).find()
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

		String f245h = underlayingMarc.getField("245", 'h');
		if (f245h == null) f245h = "";

		String f300a = underlayingMarc.getField("300", 'a');
		if (f300a == null) f300a = "";

		String f336b = underlayingMarc.getField("336", 'b');
		if (f336b == null) f336b = "";

		String f338b = underlayingMarc.getField("338", 'b');
		if (f338b == null) f338b = "";

		return ELEKTRONICKY_ZDROJ.matcher(f245h).find()
				|| (MetadataUtils.containsChar(ARRAY_ACDIJPT, f006_00) && f006_06 == 's')
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

		String f245h = underlayingMarc.getField("245", 'h');
		if (f245h == null) f245h = "";
		// AUDIO_CD
		if (isAudioCD()) return HarvestedRecordFormatEnum.AUDIO_CD;

		// AUDIO_LP
		if (GRAMOFONOVA_DESKA.matcher(f300).find()
				|| (ZVUKOVA_DESKA.matcher(f300).find() && ANALOG.matcher(f300).find())
				|| LP_OR_SP.matcher(f300a).find()
				|| ((ZVUKOVY_ZAZNAM.matcher(f245h).find() || ZVUKOVA_DESKA.matcher(f300).find())
				&& CM30.matcher(f300).find()))
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
		char ldr06 = getLeaderChar(underlayingMarc.getLeader().getTypeOfRecord());
		String f300 = underlayingMarc.getDataFields("300").toString();
		String f500 = underlayingMarc.getDataFields("500").toString();
		String f300a = underlayingMarc.getField("300", 'a');
		if (f300a == null) f300a = "";
		String f245h = underlayingMarc.getField("245", 'h');
		if (f245h == null) f245h = "";

		for (String data : new String[]{f300, f500}) {
			if (KOMPAKTNI_DISK.matcher(data).find()
					|| (CD_R.matcher(data).find() && !CD_ROM.matcher(data).find()))
				return true;
		}
		return ZVUKOVE_CD.matcher(f300).find()
				|| (CD.matcher(f300a).find() && !CD_ROM.matcher(f300a).find())
				|| (ZVUKOVA_DESKA.matcher(f300).find() && DIGITAL_OR_12CM.matcher(f300).find())
				|| ((MetadataUtils.containsChar(ARRAY_IJ, ldr06) || ZVUKOVY_ZAZNAM.matcher(f245h).find())
				&& DIGITAL_OR_12CM.matcher(f300).find())
				|| ZVUKOVY_DISK.matcher(f300).find();
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
				|| f006_00 == 'r'
				|| (f007_00 == 'z' && f336b.equalsIgnoreCase("zzz"))
				|| (f337b.equalsIgnoreCase("x") || f337b.equalsIgnoreCase("z"))
				|| f338b.equalsIgnoreCase("zu");
	}

	protected HarvestedRecordFormatEnum getThesis() {
		List<String> all502a = underlayingMarc.getFields("502", 'a');
		if (all502a.isEmpty()) return null;

		for (String f502a : all502a) {
			if (BACHELOR.matcher(f502a).find()) return THESIS_BACHELOR;
			if (MASTER.matcher(f502a).find()) return THESIS_MASTER;
			if (ADVANCED_MASTER.matcher(f502a).find()) return THESIS_ADVANCED_MASTER;
			if (DISSERTATION.matcher(f502a).find()) return THESIS_DISSERTATION;
			if (HABILITATION.matcher(f502a).find()) return THESIS_HABILITATION;
		}

		return THESIS_OTHER;
	}

	protected boolean isBoardGames(List<HarvestedRecordFormatEnum> formats) {
		String f650a = underlayingMarc.getFields("650", 'a').toString();
		if (f650a == null) f650a = "";
		String f653a = underlayingMarc.getFields("653", 'a').toString();
		if (f653a == null) f653a = "";
		String f655a = underlayingMarc.getField("655", ' ', 'a');
		if (f655a == null) f655a = "";
		String f072a = underlayingMarc.getField("072", ' ', 'a');
		if (f072a == null) f072a = "";
		return (formats.contains(BOOKS) && BOARD_GAMES.matcher(f655a).find())
				|| ((formats.contains(VISUAL_DOCUMENTS) || formats.contains(OTHER_OTHER))
				&& (BOARD_GAMES.matcher(f650a).find() || BOARD_GAMES.matcher(f653a).find()
				|| BOARD_GAMES.matcher(f655a).find() || f072a.equals("794")));
	}

	protected char getLeaderChar(final char c) {
		return Character.toLowerCase(c);
	}

	@Override
	public List<HarvestedRecordFormatEnum> getDetectedFormatList() {
		List<HarvestedRecordFormatEnum> hrf = new ArrayList<>();

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

		if (isBook()) {
			if (isMacan()) {
				hrf.add(HarvestedRecordFormatEnum.BLIND_BRAILLE);
				return hrf;
			}
			hrf.add(BOOKS);
		}
		HarvestedRecordFormatEnum thesis;
		if ((thesis = getThesis()) != null) return Collections.singletonList(thesis);
		if (isPeriodical()) hrf.add(HarvestedRecordFormatEnum.PERIODICALS);
		if (isArticle()) hrf.add(HarvestedRecordFormatEnum.ARTICLES);
		if (isArticle773()) return Collections.singletonList(HarvestedRecordFormatEnum.ARTICLES);
		if (isMap()) return Collections.singletonList(HarvestedRecordFormatEnum.MAPS);
		if (isMusicalScores()) hrf.add(HarvestedRecordFormatEnum.MUSICAL_SCORES);
		if (isVisualDocument()) hrf.add(HarvestedRecordFormatEnum.VISUAL_DOCUMENTS);
		if (isMicroform()) hrf.add(HarvestedRecordFormatEnum.OTHER_MICROFORMS);
		if (isBlindBraille()) hrf.add(HarvestedRecordFormatEnum.BLIND_BRAILLE);
		HarvestedRecordFormatEnum video = getVideoDocument();
		if (video != null) {
			if (isVideoDVD()) {
				if (!hrf.contains(HarvestedRecordFormatEnum.VIDEO_DVD)) hrf.add(HarvestedRecordFormatEnum.VIDEO_DVD);
			} else hrf.add(video);
		}
		if (isComputerCarrier()) hrf.add(HarvestedRecordFormatEnum.OTHER_COMPUTER_CARRIER);
		if (isOthers()) hrf.add(HarvestedRecordFormatEnum.OTHER_OTHER);
		if (hrf.isEmpty()) hrf.add(HarvestedRecordFormatEnum.OTHER_OTHER);
		if (isBoardGames(hrf)) return Collections.singletonList(HarvestedRecordFormatEnum.BOARD_GAMES);
		if (hrf.size() > 1 && hrf.contains(BOOKS)) {
			hrf.remove(BOOKS);
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
			if (cf != null && cf.length() >= 38) {
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

	/**
	 * 041a or 041d or 008 char 35-37
	 *
	 * @return List of {@link BLLanguage}
	 */
	@Override
	public List<BLLanguage> getBiblioLinkerLanguages() {
		Set<BLLanguage> result = new HashSet<>();
		for (String lang : getFields("041a")) {
			lang = lang.trim();
			if (lang.length() == 3) {
				result.add(BLLanguage.create(lang.toLowerCase()));
			}
		}
		if (!result.isEmpty()) return new ArrayList<>(result);
		for (String lang : getFields("041d")) {
			lang = lang.trim();
			if (lang.length() == 3) {
				result.add(BLLanguage.create(lang.toLowerCase()));
			}
		}
		if (!result.isEmpty()) return new ArrayList<>(result);
		String cf = underlayingMarc.getControlField("008");
		if (cf != null && cf.length() >= 38) {
			String substr = cf.substring(35, 38).trim();
			if (substr.length() == 3) {
				result.add(BLLanguage.create(substr.toLowerCase()));
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
		if (!matchFilterEbooks()) return false;
		// more rules in institution specific classes
		return true;
	}

	/**
	 * only bookport/palmknihy 856 a no 996 then delete record
	 *
	 * @return boolean
	 */
	@Override
	public boolean matchFilterEbooks() {
		for (String str : underlayingMarc.getFields("856", 'u')) {
			if (!EBOOKS_URL.matcher(str).find()) return true;
		}
		return underlayingMarc.getFields("856", 'u').isEmpty()
				|| !underlayingMarc.getDataFields("996").isEmpty();
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
				logger.debug(String.format("Invalid ISMN: %s", nfe.getMessage()));
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
		return getUrls(availability,null);
	}

	protected List<String> getUrls(String availability, String defaultComment) {
		List<String> result = new ArrayList<>();

		for (DataField df : underlayingMarc.getDataFields("856")) {
			if (df.getSubfield('u') == null) {
				continue;
			}
			String link = df.getSubfield('u').getData();
			String comment = "";
			if (defaultComment != null)
				comment = defaultComment;
			else {
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
			}
			result.add(MetadataUtils.generateUrl(harvestedRecord.getHarvestedFrom().getIdPrefix(),
					availability, link, comment));
		}
		return filterEbookUrls(result);
	}

	@Override
	public List<String> filterEbookUrls(List<String> urls) {
		List<String> ebooks = new ArrayList<>();
		List<String> others = new ArrayList<>();
		for (String url : urls) {
			if (EBOOKS_URL.matcher(url).find()) ebooks.add(url);
			else others.add(url);
		}
		if (ebooks.isEmpty()) return urls;
		if (!others.isEmpty() || !underlayingMarc.getDataFields("996").isEmpty()) return others;
		return urls;
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
				logger.debug(String.format("Invalid EAN: %s", nfe.getMessage()));
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
	 * @return all 245anp, if contains subfield 'b'
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
					boolean similarity = MetadataUtils.similarityEnabled(df, titleText);
					titleText = RomanNumeralsUtils.getRomanNumerals(titleText);
					for (Pattern pattern : TITLE_REMOVE_WORDS_PATTERNS) {
						titleText = CleaningUtils.replaceAll(titleText, pattern, "");
					}
					results.add(ShortTitle.create(titleText, ++shortTitleCounter, similarity));
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
			if (Chars.contains(SUBFIELDS, subfield.getCode())) {
				if (builder.length() != 0) builder.append(" ");
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
		long i = 0L;
		for (DataField df : underlayingMarc.getDataFields("028")) {
			if (df.getSubfield('a') != null) {
				String result = PUBLISHER_NUMBER_PATTERN.matcher(df.getSubfield('a').getData().toLowerCase()).replaceAll("");
				result = MetadataUtils.shorten(result, EFFECTIVE_LENGTH_PUBLISHER_NUMBER);
				if (result.isEmpty()) continue;
				results.add(new PublisherNumber(result, ++i));
			}
		}
		return results;
	}

	@Override
	public String getSourceInfoXZ() {
		return underlayingMarc.getField("773", 'x') != null ? underlayingMarc.getField("773", 'x') :
				underlayingMarc.getField("773", 'z');
	}

	@Override
	public String getSourceInfoT() {
		return underlayingMarc.getField("773", 't');
	}

	@Override
	public String getSourceInfoG() {
		String result = underlayingMarc.getField("773", 'g');
		if (result == null) return null;
		for (Pair<Pattern, String> pattern : SOURCE_INFO_G_PATTERNS) {
			result = CleaningUtils.replaceAll(result, pattern.getLeft(), pattern.getRight());
		}
		return result;
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
		if (!getDetectedFormatList().contains(BOOKS)) return null;
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
		if (!getDetectedFormatList().contains(BOOKS)) return null;
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
		if (!getDetectedFormatList().contains(BOOKS)) return Collections.emptySet();
		Set<AnpTitle> results = new HashSet<>();
		for (DataField df : underlayingMarc.getDataFields("245")) {
			String titleText = parseTitleValue(df, SHORT_TITLE_SUBFIELDS);
			if (!titleText.isEmpty()) {
				boolean similarity = MetadataUtils.similarityEnabled(df, titleText);
				titleText = RomanNumeralsUtils.getRomanNumerals(titleText);
				for (Pattern pattern : TITLE_REMOVE_WORDS_PATTERNS) {
					titleText = CleaningUtils.replaceAll(titleText, pattern, "");
				}
				results.add(AnpTitle.create(titleText, similarity));
			}
		}
		return results;
	}

	/**
	 * 765a, 210a, 222a
	 * 130anp, 730anp - without text in parentheses in subfield a
	 *
	 * @return list of {@link BLTitle}
	 */
	@Override
	public List<BLTitle> getBiblioLinkerTitle() {
		List<BLTitle> result = new ArrayList<>();
		for (DataField df : underlayingMarc.getDataFields("765")) {
			String titleText = parseTitleValue(df, new char[]{'t'});
			if (!titleText.isEmpty()) {
				result.add(BLTitle.create(titleText));
			}
		}
		for (String tag : new String[]{"210", "222"}) {
			for (DataField df : underlayingMarc.getDataFields(tag)) {
				String titleText = parseTitleValue(df, new char[]{'a'});
				if (!titleText.isEmpty()) {
					result.add(BLTitle.create(titleText));
				}
			}
		}
		for (String tag : new String[]{"130", "730"}) {
			for (DataField df : underlayingMarc.getDataFields(tag)) {
				String titleText = parseTitleValue(df, new char[]{'a'});
				Matcher matcher;
				if ((matcher = FIELD130A.matcher(titleText)).matches()) titleText = matcher.group(1);
				titleText += parseTitleValue(df, new char[]{'n', 'p'});
				if (!titleText.isEmpty()) {
					result.add(BLTitle.create(titleText));
				}
			}
		}
		return result;
	}

	private String getFirstField(String fields) {
		for (String field : fields.split(":")) {
			String tag = field.substring(0, 3);
			String codes = field.substring(3);
			String result = underlayingMarc.getField(tag, codes.toCharArray());
			if (result != null) return result;
		}
		return null;
	}

	private List<String> getFields(String fields) {
		List<String> results = new ArrayList<>();
		for (String field : fields.split(":")) {
			String tag = field.substring(0, 3);
			String codes = field.substring(3);
			results.addAll(underlayingMarc.getFields(tag, " ", codes.toCharArray()));
		}
		return results;
	}

	/**
	 * get author for biblio linker
	 *
	 * @return String
	 */
	@Override
	public String getBiblioLinkerAuthor() {
		String result;
		if (!Collections.disjoint(getDetectedFormatList(), BL_AUTHOR_VIDEO)) {
			result = getBiblioLinkerAuthorPart("1", true);
			if (result != null) return result;
			result = getBiblioLinkerAuthorPart("7", true);
			if (result != null) return result;
		}
		result = getBiblioLinkerAuthorPart("1", false);
		if (result != null) return result;
		return getBiblioLinkerAuthorPart("7", false);
	}

	private String getBiblioLinkerAuthorPart(String tagPrefix, boolean filter) {
		for (String tag : new String[]{tagPrefix + "00", tagPrefix + "10", tagPrefix + "11"}) {
			for (DataField df : underlayingMarc.getDataFields(tag)) {
				if (filter && (df.getSubfield('4') == null
						|| !BL_AUTHOR_RELATIONSHIP.contains(df.getSubfield('4').getData()))) {
					continue;
				}
				if (df.getSubfield('7') != null) return df.getSubfield('7').getData();
			}
		}
		for (DataField df : underlayingMarc.getDataFields(tagPrefix + "00")) {
			if (filter && (df.getSubfield('4') == null
					|| !BL_AUTHOR_RELATIONSHIP.contains(df.getSubfield('4').getData()))) {
				continue;
			}
			if (df.getSubfield('a') == null) continue;
			String result = df.getSubfield('a').getData();
			if (df.getSubfield('d') != null) {
				Matcher matcher = YEAR_PATTERN.matcher(df.getSubfield('d').getData());
				if (matcher.find()) result += matcher.group(0);
			}
			if (result != null) return result;
		}
		for (DataField df : underlayingMarc.getDataFields(tagPrefix + "10")) {
			if (filter && (df.getSubfield('4') == null
					|| !BL_AUTHOR_RELATIONSHIP.contains(df.getSubfield('4').getData()))) {
				continue;
			}
			StringBuilder result = new StringBuilder();
			for (char code : new char[]{'a', 'b', 'c', 'd', 'n'}) {
				result.append(df.getSubfield(code) != null ? df.getSubfield(code) : "");
			}
			if (result.length() > 0) return result.toString();
		}
		for (DataField df : underlayingMarc.getDataFields(tagPrefix + "11")) {
			if (filter && (df.getSubfield('4') == null
					|| !BL_AUTHOR_RELATIONSHIP.contains(df.getSubfield('4').getData()))) {
				continue;
			}
			StringBuilder result = new StringBuilder();
			for (char code : new char[]{'a', 'c', 'd', 'n'}) {
				result.append(df.getSubfield(code) != null ? df.getSubfield(code) : "");
			}
			if (result.length() > 0) return result.toString();
		}
		return null;
	}

	/**
	 * first of 264b, 260b, 260f, 928a
	 *
	 * @return String
	 */
	@Override
	public String getBiblioLinkerPublisher() {
		return getFirstField("264b:260b:260f:928a");
	}

	/**
	 * first of 440a, 490a
	 *
	 * @return String
	 */
	@Override
	public String getBiblioLinkerSeries() {
		return getFirstField("440a:490a");
	}

	/**
	 * 787t when subfield $i matches {@link #Z_CYKLU}
	 * OR 24[056]$a, 24[056]$ab (IF $p OR $n)
	 *
	 * @return list of {@link BlCommonTitle}
	 */
	@Override
	public List<BlCommonTitle> getBiblioLinkerCommonTitle() {
		List<BlCommonTitle> results = new ArrayList<>();
		for (DataField df : underlayingMarc.getDataFields("787")) {
			if (df.getSubfield('t') != null && df.getSubfield('i') != null
					&& Z_CYKLU.matcher(df.getSubfield('i').getData()).matches()) {
				results.add(BlCommonTitle.create(df.getSubfield('t').getData()));
			}
		}
		if (!results.isEmpty()) return results;
		for (String tag : new String[]{"240", "245", "246"}) {
			for (DataField df : underlayingMarc.getDataFields(tag)) {
				if (df.getSubfield('n') == null && df.getSubfield('p') == null) continue;
				if (df.getSubfield('a') == null) continue;
				results.add(BlCommonTitle.create(df.getSubfield('a').getData()));
				if (df.getSubfield('b') != null) {
					results.add(BlCommonTitle.create(df.getSubfield('a').getData() + df.getSubfield('b').getData()));
				}
			}
		}
		return results;
	}

	/**
	 * first 3 values alphabetically
	 * map: 650 ind1!=2 subfield 7, subfield 7 not contains any of {@link #BL_TOPIC_KEY_STOP_WORDS_650}
	 * other: 651 subfield 7
	 * <p>
	 * first of 072a
	 *
	 * @return List of {@link BLTopicKey}
	 */
	@Override
	public List<BLTopicKey> getBiblioLinkerTopicKey() {
		List<BLTopicKey> results = new ArrayList<>();
		Set<String> topicKey = new TreeSet<>();
		if (isMap()) {
			for (DataField df : underlayingMarc.getDataFields("650")) {
				if (df.getIndicator1() != '2' && df.getSubfield('7') != null) {
					if (BL_TOPIC_KEY_STOP_WORDS_650.contains(df.getSubfield('7').getData())) continue;
					if (!topicKey.contains(df.getSubfield('7').getData())) topicKey.add(df.getSubfield('7').getData());
				}
			}
		}
		for (String topicValue : getFields("6517")) {
			if (!topicKey.contains(topicValue)) topicKey.add(topicValue);
		}
		results.addAll(topicKey.stream().limit(3).map(BLTopicKey::create).collect(Collectors.toList()));
		String conspectus = getFirstField("072a");
		if (conspectus != null) results.add(BLTopicKey.create(conspectus));
		return results;
	}

	/**
	 * first of 100$7, 110$7, 111$7, 100$ad(YYYY), 110$abcdn, 111$acdn, 700$7, 710$7, 711$7, 700$ad(YYYY), 710$abcdn, 711$acdn
	 * first 3: 700$7, 710$7, 711$7, 700$ad(YYYY), 710$abcdn, 711$acdn and $4 exists in {@link #ENTITY_RELATIONSHIP}
	 * first 3: 600$7, 610$7, 611$7, 600$ad(YYYY), 610$abcdn, 611$acdn
	 * 100u, 700u, 314a
	 *
	 * @return List of {@link BLEntity}
	 */
	@Override
	public List<BLEntity> getBiblioLinkerEntity() {
		Set<String> results = new HashSet<>();
		results.addAll(getBiblioLinkerEntityPart("1", false).stream().limit(1).collect(Collectors.toSet()));
		if (results.isEmpty()) {
			results.addAll(getBiblioLinkerEntityPart("7", false).stream().limit(1).collect(Collectors.toSet()));
		}
		results.addAll(getBiblioLinkerEntityPart("7", true).stream().limit(3).collect(Collectors.toSet()));
		results.addAll(getBiblioLinkerEntityPart("6", false).stream().limit(3).collect(Collectors.toSet()));
		results.addAll(getFields("100u:700u:314a"));
		return results.stream().filter(s -> !s.contains("ebrary")).map(BLEntity::create).collect(Collectors.toList());
	}

	/**
	 * @param tag    first char of tag
	 * @param filter subfield $4 exists in {@link #ENTITY_RELATIONSHIP}
	 * @return Set of entities
	 */
	private Set<String> getBiblioLinkerEntityPart(final String tag, final boolean filter) {
		Set<String> results = new LinkedHashSet<>();
		results.addAll(getBiblioLinkerEntityValue(tag + "007:" + tag + "107:" + tag + "117", filter)
				.stream().limit(3).collect(Collectors.toList()));
		for (DataField df : underlayingMarc.getDataFields(tag + "00")) {
			if (filter && (df.getSubfield('4') == null
					|| !ENTITY_RELATIONSHIP.contains(df.getSubfield('4').getData()))) {
				continue;
			}
			if (df.getSubfield('a') == null) continue;
			String temp = df.getSubfield('a').getData();
			if (df.getSubfield('d') != null) {
				Matcher matcher = YEAR_PATTERN.matcher(df.getSubfield('d').getData());
				if (matcher.find()) temp += matcher.group(0);
			}
			results.add(temp);
		}
		results.addAll(getBiblioLinkerEntityValue(tag + "10abcdn:" + tag + "11acdn", filter));
		return results;
	}

	/**
	 * get values from fields
	 *
	 * @param fields format: tag + subfields, e.g. 100ab, separated by colon, e.g. 100ab:700ab
	 * @param filter subfield $4 exists in {@link #ENTITY_RELATIONSHIP}
	 * @return Set of entities
	 */
	private Set<String> getBiblioLinkerEntityValue(final String fields, final boolean filter) {
		Set<String> results = new LinkedHashSet<>();
		for (String field : fields.split(":")) {
			String tag = field.substring(0, 3);
			String codes = field.substring(3);
			for (DataField df : underlayingMarc.getDataFields(tag)) {
				if (filter && (df.getSubfield('4') == null
						|| !ENTITY_RELATIONSHIP.contains(df.getSubfield('4').getData()))) {
					continue;
				}
				StringBuilder entityValue = new StringBuilder();
				for (char c : codes.toCharArray()) {
					if (df.getSubfield(c) != null) entityValue.append(df.getSubfield(c).getData());
				}
				if (entityValue.length() > 0) results.add(entityValue.toString());
			}
		}
		return results;
	}

	@Override
	public String getAuthorDisplay() {
		List<DataField> list = underlayingMarc.getDataFields("100");
		if (list.isEmpty()) return null;
		DataField df = list.get(0);
		String name = SolrUtils.getNameForDisplay(df);
		if (name != null && name.isEmpty()) return null;
		else return name;
	}

	@Override
	public String getTitleDisplay() {
		List<DataField> dfs = underlayingMarc.getDataFields("245");
		if (dfs.isEmpty()) return null;
		DataField df = dfs.get(0);

		final char titleSubfields[] = {'a', 'b', 'n', 'p'};
		final char sfhPunctuation[] = {'.', ',', ':'};
		char endCharH = ' ';
		StringBuilder sb = new StringBuilder();

		for (Subfield sf : df.getSubfields()) {
			// get last punctuation from 'h'
			if (sf.getCode() == 'h') {
				String data = sf.getData().trim();
				if (!data.isEmpty()) {
					if (Chars.contains(sfhPunctuation, data.charAt(data.length() - 1))) {
						endCharH = data.charAt(data.length() - 1);
					}
				}
			} else if (Chars.contains(titleSubfields, sf.getCode())) {
				// print punctuation from h
				if (endCharH != ' ') {
					sb.append(endCharH);
					sb.append(' ');
					endCharH = ' ';
				}
				sb.append(sf.getData());
				sb.append(' ');
			} else endCharH = ' ';
		}
		return SolrUtils.removeEndPunctuation(sb.toString());
	}

	@Override
	public List<Uuid> getUuids() {
		Set<Uuid> results = new HashSet<>();
		for (String tag : new String[]{"856", "911"}) {
			for (String url : underlayingMarc.getFields(tag, 'u')) {
				Matcher matcher = UUID_PATTERN.matcher(url);
				if (matcher.find()) {
					results.add(Uuid.create(matcher.group(0)));
				}
			}
		}
		return new ArrayList<>(results);
	}

	/**
	 * allowed sources, with allowed formats and absent 996
	 *
	 * @return boolean
	 */
	@Override
	public boolean isZiskej() {
		boolean result = harvestedRecord.getHarvestedFrom().isZiskejEnabled()
				&& !underlayingMarc.getDataFields("996").isEmpty()
				&& !Collections.disjoint(getDetectedFormatList(), ZISKEJ_FORMAT_ALLOWED);
		if (!result) return false;
		for (DataField df : underlayingMarc.getDataFields("996")) {
			if (df.getSubfield('q') != null && df.getSubfield('q').getData().equals("0")) continue;
			if (df.getSubfield('s') != null && ZISKEJ_ABSENT_996.contains(df.getSubfield('s').getData().toUpperCase())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Long getLoanRelevance() {
		Long count = 0L;
		boolean exists = false;
		for (DataField df : underlayingMarc.getDataFields("996")) {
			if (df.getSubfield('n') != null)
				try {
					count += Long.valueOf(df.getSubfield('n').getData());
					exists = true;
				} catch (NumberFormatException ignored) {
				}
		}
		return exists ? count : null;
	}

	/**
	 * allowed sources, with allowed formats
	 *
	 * @return boolean
	 */
	@Override
	public boolean isEdd() {
		return harvestedRecord.getHarvestedFrom().isZiskejEnabled()
				&& !Collections.disjoint(getDetectedFormatList(), EDD_FORMAT_ALLOWED);
	}

	@Override
	public String getPalmknihyId() {
		Matcher matcher;
		for (String url : underlayingMarc.getFields("856", 'u')) {
			matcher = PALMKNIHY_ID.matcher(url);
			if (matcher.matches()) return matcher.group(1);
		}
		return null;
	}

	@Override
	public boolean dedupFilter() {
		for (Title title : getTitle()) {
			for (Pattern stopWord : DEDUP_STOP_WORDS) {
				if (stopWord.matcher(title.getTitleStr()).find()) return false;
			}
		}
		return true;
	}
}
