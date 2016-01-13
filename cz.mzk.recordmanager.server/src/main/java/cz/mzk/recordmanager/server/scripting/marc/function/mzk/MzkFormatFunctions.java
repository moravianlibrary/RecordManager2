package cz.mzk.recordmanager.server.scripting.marc.function.mzk;

import java.util.List;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Subfield;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.scripting.marc.MarcFunctionContext;
import cz.mzk.recordmanager.server.scripting.marc.function.MarcRecordFunctions;

@Component
public class MzkFormatFunctions implements MarcRecordFunctions {

	private static final String UNKNOWN_FORMAT = "Unknown";

	private static enum MZKFormat {

		PHOTOGRAPHY_FORMAT("Photography") {

			@Override
			public boolean match(MarcFunctionContext ctx) {
				List<DataField> fields072 = ctx.record().getDataFields("072");
				for (DataField field : fields072) {
					Subfield sa = field.getSubfield('a');
					Subfield sx = field.getSubfield('x');
					if (match(sa, "77") && match(sx, "fotografie")) {
						return true;
					}
				}
				List<DataField> fields655 = ctx.record().getDataFields("072");
				for (DataField field : fields655) {
					Subfield sa = field.getSubfield('a');
					Subfield sx = field.getSubfield('x');
					if (match(sa, "fotografie") && match(sx, "fd132277")) {
						return true;
					}
				}
				return false;
			}

		},

		ELECTRONIC("Electronic") {

			@Override
			public boolean match(MarcFunctionContext ctx) {
				for (String sf : ctx.record().getFields("245", 'h')) {
					if (sf.contains("[electronic resource]")) {
						return true;
					}
				}
				return false;
			}

		},

		ARTICLE("Article") {

			@Override
			public boolean match(MarcFunctionContext ctx) {
				String format = ctx.record().getControlField("990");
				return (format != null && format.equals("AZ"));
			}

		},

		NORM("Norm") {

			@Override
			public boolean match(MarcFunctionContext ctx) {
				return (ctx.record().getField("991", 'n') != null);
			}

		},

		LAWS_OR_OTHERS("LawsOrOthers") {

			@Override
			public boolean match(MarcFunctionContext ctx) {
				Leader leader = ctx.record().getLeader();
				if (leader == null) {
					return false;
				}
				String field000 = leader.toString();
				if (field000 == null || field000.length() < 8) {
					return false;
				}
				String format = field000.substring(5, 8);
				return (format.equals("nai") || format.equals("cai"));
			}

		};

		private final String description;

		private MZKFormat(String description) {
			this.description = description;
		}

		public abstract boolean match(MarcFunctionContext ctx);

		protected boolean match(Subfield subfield, String value) {
			if (subfield == null || subfield.getData() == null) {
				return false;
			}
			return subfield.getData().toLowerCase().trim().equals(value);
		}

	}

	public String getMZKFormat(MarcFunctionContext ctx) {
		for (MZKFormat format : MZKFormat.values()) {
			if (format.match(ctx)) {
				return format.description;
			}
		}

		if (ctx.record().getLeader() == null) {
			return UNKNOWN_FORMAT;
		}
		String leader = ctx.record().getLeader().toString();
		if (leader == null || leader.length() < 7) {
			return UNKNOWN_FORMAT;
		}

		// check the Leader at position 6
		char leaderBit6 = Character.toUpperCase(leader.charAt(6));
		switch (leaderBit6) {
			case 'C':
			case 'D':
				return "MusicalScore";
			case 'E':
			case 'F':
				return "Map";
			case 'G':
				return "Slide";
			case 'I':
				return "SoundRecording";
			case 'J':
				return "MusicRecording";
			case 'K':
				return "Photo";
			case 'M':
				return "Electronic";
			case 'O':
			case 'P':
				return "Kit";
			case 'R':
				return "PhysicalObject";
			case 'T':
				return "Manuscript";
		}

		// check the Leader at position 7
		char leaderBit7 = Character.toUpperCase(leader.charAt(7));
		switch (leaderBit7) {
			case 'M':
				return "Book";
			case 'S':
				return "NewspaperOrJournal";
		}

		return UNKNOWN_FORMAT;
	}

}
