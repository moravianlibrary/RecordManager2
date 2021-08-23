package cz.mzk.recordmanager.server.marc;

import cz.mzk.recordmanager.server.util.MarcRecordUtils;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum SubfieldExtractionMethod {

	JOINED {

		private SubfieldJoiner joiner = new SubfieldJoiner() {

			@Override
			public List<String> extract(DataField df, String separator, char... subfields) {
				return Collections.singletonList(MarcRecordUtils.parseSubfields(df, separator, subfields));
			}
		};

		@Override
		public SubfieldJoiner getJoiner() {
			return joiner;
		}

	},

	SEPARATED {

		private SubfieldJoiner joiner = new SubfieldJoiner() {

			@Override
			public List<String> extract(DataField df, String separator, char... subfields) {
				List<String> result = new ArrayList<String>();
				for (char subfield : subfields) {
					List<Subfield> sfl = df.getSubfields(subfield);
					if (sfl != null && !sfl.isEmpty()) {
						for (Subfield sf : sfl) {
							result.add(sf.getData());
						}
					}
				}
				return result;
			}

		};

		@Override
		public SubfieldJoiner getJoiner() {
			return joiner;
		}

	},

	ALL_SUBFIELDS_JOINED {

		private final SubfieldJoiner joiner = new SubfieldJoiner() {

			@Override
			public List<String> extract(DataField df, String separator, char... subfields) {
				StringBuilder sb = new StringBuilder();
				for (Subfield sf : df.getSubfields()) {
					if (sf != null) {
						sb.append(separator);
						sb.append(sf.getData());
					}
				}
				return Collections.singletonList(sb.toString().trim());
			}
		};

		@Override
		public SubfieldJoiner getJoiner() {
			return joiner;
		}

	};

	public abstract SubfieldJoiner getJoiner();

}