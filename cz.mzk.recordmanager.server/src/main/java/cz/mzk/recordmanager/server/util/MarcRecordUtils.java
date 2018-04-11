package cz.mzk.recordmanager.server.util;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;

import java.util.List;

public class MarcRecordUtils {

	private static final String EMPTY_STRING = "";

	/**
	 * join subfields string from datafield
	 *
	 * @param df        {@link DataField}
	 * @param separator of subfield data
	 * @param subfields subfield codes to be returned
	 * @return joined {@link Subfield} data as String
	 */
	public static String parseSubfields(final DataField df, final String separator, final char... subfields) {
		StringBuilder sb = new StringBuilder();
		String sep = EMPTY_STRING;
		for (char subfield : subfields) {
			List<Subfield> sfl = df.getSubfields(subfield);
			if (sfl != null && !sfl.isEmpty()) {
				for (Subfield sf : sfl) {
					sb.append(sep);
					sb.append(sf.getData());
					sep = separator;
				}
			}
		}
		return sb.toString();
	}

}
