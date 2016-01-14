package cz.mzk.recordmanager.server.scripting.marc.function.mzk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import cz.mzk.recordmanager.server.scripting.marc.MarcFunctionContext;
import cz.mzk.recordmanager.server.scripting.marc.function.MarcRecordFunctions;

@Component
public class MzkOtherFunctions implements MarcRecordFunctions {

	public String getMZKAcquisitionDate(MarcFunctionContext ctx) {
		String f991b = ctx.record().getField("991", 'b');
		String f991c = ctx.record().getField("991", 'c');
		if (f991b == null || f991c == null || !f991c.startsWith("NOV")) {
			return null;
		}
		f991b = f991b.trim();
		if (f991b.length() > 6) {
			f991b = f991b.substring(0, 6);
		}
		return (NumberUtils.isNumber(f991b)) ? f991b : null;
	}

	private static final String HIDDEN = "hidden";

	private static final String VISIBLE = "visible";

	public String getMZKVisible(MarcFunctionContext ctx) {
		// MZK field is remapped to 991 in OAI
		String mzkHidden = ctx.record().getField("991", 's');
		if (mzkHidden != null && mzkHidden.startsWith("SKRYTO")) {
			return HIDDEN;
		}
		// FMT field is remapped to 990 in OAI and is control field
		String format = ctx.record().getField("990", 'a');
		if (format != null && format.startsWith("AZ")) {
			return HIDDEN;
		}
		// STA field is remapped to 992 in OAI
		String sta = ctx.record().getField("992", 'a');
		if (sta != null && (sta.startsWith("SUPPRESSED") || sta.startsWith("SKRYTO"))) {
			return HIDDEN;
		}
		// BAS field is remapped to 995 in OAI (acquisition order)
		String bas = ctx.record().getField("995", 'a');
		if (bas != null && bas.startsWith("AK")) {
			return HIDDEN;
		}
		return VISIBLE;
	}

	public String getMZKAvailabilityId(MarcFunctionContext ctx) {
		for (DataField df : ctx.record().getDataFields("996")) {
			Subfield sfw = df.getSubfield('w');
			if (sfw != null && sfw.getData() != null && !sfw.getData().trim().isEmpty()) {
				return sfw.getData();
			}
		}
		return null;
	}

	private static final String DEFAULT_RELEVANCY = "default";

	private static final String INVALID_NORM_TEXT = "Norma je neplatná";

	private static final String INVALID_NORM_RELEVANCY = "invalid_norm";

	private static final String RUBBISH_RELEVANCY = "invalid_norm";

	public String getMZKRelevancy(MarcFunctionContext ctx) {
		String summary = ctx.record().getField("520", 'a');
		if (summary != null && summary.equals(INVALID_NORM_TEXT)) {
			return INVALID_NORM_RELEVANCY;
		}
		String sig = ctx.record().getField("910", 'b');
		if (sig != null && sig.startsWith("UP")) {
			return RUBBISH_RELEVANCY;
		}
		return DEFAULT_RELEVANCY;
	}

	public List<String> getMZKKeywords(MarcFunctionContext ctx) {
		List<String> result = new ArrayList<String>();
		result.addAll(ctx.record().getFields("600", field -> field.getIndicator2() == '7', " ", 'a', 'b', 'd', 'q', 'k', 'l', 'm', 'p', 'r', 's'));
		result.addAll(ctx.record().getFields("610", field -> field.getIndicator2() == '7', " ", 'a', 'b', 'c', 'k', 'l', 'm', 'p', 'r', 's'));
		result.addAll(ctx.record().getFields("611", field -> field.getIndicator2() == '7', " ", 'a', 'c', 'e' ,'q', 'k', 'l', 'm', 'p', 'r', 's'));
		result.addAll(ctx.record().getFields("630", field -> field.getIndicator2() == '7', " ", 'a', 'd', 'k', 'l', 'm', 'p', 'r', 's'));
		result.addAll(ctx.record().getFields("650", field -> field.getIndicator2() == '7', " ", 'a', 'v', 'x', 'y', 'z'));
		result.addAll(ctx.record().getFields("651", field -> field.getIndicator2() == '7', " ", 'a', 'v', 'x', 'y', 'z'));
		result.addAll(ctx.record().getFields("653", field -> true, " ", 'a'));
		result.addAll(ctx.record().getFields("655", field -> field.getIndicator2() == '7', " ", 'a', 'v', 'x', 'y', 'z'));
		result.addAll(ctx.record().getFields("964", field -> true, " ", 'a'));
		result.addAll(ctx.record().getFields("967", field -> true, " ", 'a', 'b', 'c'));
		return result;
	}

	private static final Map<String, Set<String>> ALLOWED_BASES = ImmutableMap.of(
				"MZK01", ImmutableSet.of("33", "44", "99"), //
				"MZK03", ImmutableSet.of("mzk", "rajhrad", "znojmo", "trebova", "dacice", "minorite")
	);

	private static final String BASE_PREFIX = "facet_base_";

	private static final String BASE_SEPARATOR = "_";

	private static final String INFO_USA_VALUE = "USA";

	private static final String INFO_USA_BASE_SUFFIX = "infoUSA";

	public List<String> getMZKBases(MarcFunctionContext ctx) {
		String idParts[] = ctx.harvestedRecord().getUniqueId().getRecordId().split("-");
		String base = idParts[0];
		List<String> result = new ArrayList<String>();
		String primaryBase = BASE_PREFIX + base;
		result.add(primaryBase);
		String secondaryBase =  ctx.record().getField("991", (base.equals("MZK01")) ? 'x' : 'k');
		if (secondaryBase != null && ALLOWED_BASES.get(base).contains(secondaryBase)) {
			result.add(primaryBase + BASE_SEPARATOR + secondaryBase);
		}
		// info USA
		for (String field : ctx.record().getFields("996", 'l')) {
			if (INFO_USA_VALUE.equals(field)) {
				result.add(primaryBase + BASE_SEPARATOR + INFO_USA_BASE_SUFFIX);
				break;
			}
		}
		return result;
	}

}
