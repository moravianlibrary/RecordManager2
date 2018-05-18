package cz.mzk.recordmanager.server.model;

import cz.mzk.recordmanager.server.hibernate.StringValueEnum;
import cz.mzk.recordmanager.server.marc.marc4j.MarcFactoryImpl;
import cz.mzk.recordmanager.server.util.CleaningUtils;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Subfield;

import java.util.regex.Pattern;

public enum ItemId implements StringValueEnum {

	ALEPH("aleph") {
		@Override
		protected String createSubfield(final DataField df, final String sigla, final String recordId) {
			String j = df.getSubfield('j') != null ? df.getSubfield('j').getData().toUpperCase() : "";
			String w = df.getSubfield('w') != null ? df.getSubfield('w').getData() : "";
			String u = df.getSubfield('u') != null ? df.getSubfield('u').getData() : "";
			if (j.isEmpty() || w.isEmpty() || u.isEmpty() || recordId == null) return null;
			return String.format(ALEPH_STRING, sigla, CleaningUtils.replaceFirst(recordId, ALEPH_CHAR, ""), j + w + u);
		}
	},

	TRE("tre") {
		@Override
		protected String createSubfield(final DataField df, final String sigla, final String recordId) {
			String w = df.getSubfield('w') != null ? df.getSubfield('w').getData() : "";
			if (w.isEmpty()) return null;
			return String.format(OTHER_STRING, sigla, w);
		}
	},

	NLK("nlk") {
		@Override
		protected String createSubfield(final DataField df, final String sigla, final String recordId) {
			String a = df.getSubfield('a') != null ? df.getSubfield('a').getData() : "";
			if (a.equals("")) return null;
			return String.format(OTHER_STRING, sigla, a);
		}
	},

	SVKUL("svkul") {
		@Override
		protected String createSubfield(final DataField df, final String sigla, final String recordId) {
			String b = df.getSubfield('b') != null ? df.getSubfield('b').getData() : "";
			if (b.equals("")) return null;
			if (b.startsWith("31480") && b.length() >= 8) {
				b = b.substring(5);
			}
			return String.format(OTHER_STRING, sigla, b);
		}
	},

	OTHER("other") {
		@Override
		protected String createSubfield(final DataField df, final String sigla, final String recordId) {
			String b = df.getSubfield('b') != null ? df.getSubfield('b').getData() : "";
			if (b.equals("")) return null;
			return String.format(OTHER_STRING, sigla, b);
		}
	};

	private static final MarcFactory MARC_FACTORY = new MarcFactoryImpl();
	private static final char ITEM_ID_SUBFIELD_CHAR = 't';
	private static final String ALEPH_STRING = "%s.%s.%s";
	private static final String OTHER_STRING = "%s.%s";
	private static final Pattern ALEPH_CHAR = Pattern.compile("-");
	private final String value;

	ItemId(String name) {
		this.value = name;
	}

	public static Subfield getItemIdSubfield(final ItemId type, final DataField df, final String sigla, final String recordId) {
		String itemId = type.createSubfield(df, sigla, recordId);
		return itemId == null ? null : MARC_FACTORY.newSubfield(ITEM_ID_SUBFIELD_CHAR, itemId);
	}

	@Override
	public String getValue() {
		return value;
	}

	protected abstract String createSubfield(final DataField df, final String sigla, final String recordId);

}
