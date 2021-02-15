package cz.mzk.recordmanager.server.metadata.institutions;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.metadata.MetadataMarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.util.CleaningUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CelitebibMarcMetadataRecord extends MetadataMarcRecord {

	private static final Pattern SOURCE_INFO_T =
			Pattern.compile("(?:r|roč).([0-9\\s]+),\\s*([0-9]{4}),\\s*č.\\s*([0-9/]+),\\s*s.\\s*([0-9-]+)", Pattern.CASE_INSENSITIVE);
	private static final String PARSED_SOURCE_INFO_T = "Roč. %s, č. %s (%s), s. %s";

	private static final List<Pair<Pattern, String>> CLEAN_SOURCE_INFO_G = new ArrayList<>();

	static {
		CLEAN_SOURCE_INFO_G.add(Pair.of(Pattern.compile(",[^,]*p[rř][ií]l[^,]*,", Pattern.CASE_INSENSITIVE), ","));
		CLEAN_SOURCE_INFO_G.add(Pair.of(Pattern.compile("[\\[\\](){}]+", Pattern.CASE_INSENSITIVE), ""));
		CLEAN_SOURCE_INFO_G.add(Pair.of(Pattern.compile(",\\s*(?:\\d{1,2}\\.\\s*\\d{1,2}\\.|" +
				"\\d{1,2}\\.\\s*\\d{1,2}\\.\\s*\\d{4}|\\d{1,2}\\.\\s*-|\\s*-\\s*)+\\s*,", Pattern.CASE_INSENSITIVE), ","));
		CLEAN_SOURCE_INFO_G.add(Pair.of(Pattern.compile(",\\s*(?:leden|[uú]nor|b[rř]ezen|duben|kv[eě]ten|[cč]erven|[cč]ervenec" +
				"|srpen|z[aá][rř][ií]|[rř][ií]jen|listopad|prosinec|jaro|l[eé]to|podzim|zima|/)+(\\s*[\\d]{4})*\\s*,", Pattern.CASE_INSENSITIVE), ","));
	}

	public CelitebibMarcMetadataRecord(MarcRecord underlayingMarc, HarvestedRecord hr) {
		super(underlayingMarc, hr);
	}

	@Override
	public String getSourceInfoG() {
		String result = super.getSourceInfoG();
		if (result == null) return null;
		for (Pair<Pattern, String> clean : CLEAN_SOURCE_INFO_G) {
			result = CleaningUtils.replaceAll(result, clean.getKey(), clean.getValue());
		}
		Matcher matcher = SOURCE_INFO_T.matcher(result);
		if (matcher.matches()) {
			result = String.format(PARSED_SOURCE_INFO_T, matcher.group(1).trim(), matcher.group(3), matcher.group(2),
					matcher.group(4));
		}
		return result;
	}
}
