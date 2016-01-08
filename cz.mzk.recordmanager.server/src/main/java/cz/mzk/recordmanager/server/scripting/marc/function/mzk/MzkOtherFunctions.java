package cz.mzk.recordmanager.server.scripting.marc.function.mzk;

import org.apache.commons.lang3.math.NumberUtils;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;
import org.springframework.stereotype.Component;

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

	private static final String VISIBLE = "hidden";

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

}
