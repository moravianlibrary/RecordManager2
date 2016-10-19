package cz.mzk.recordmanager.server.marc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;

public enum SubfieldExtractionMethod {

	JOINED {

		private SubfieldJoiner joiner = new SubfieldJoiner() {

			private static final String EMPTY_STRING = "";

			@Override
			public List<String> extract(DataField df, String separator,
					char... subfields) {
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
				return Collections.singletonList(sb.toString());
			}

		};

		@Override
		public SubfieldJoiner getJoiner() {
			return joiner;
		}

	},

	SEPARATED  {

		private SubfieldJoiner joiner = new SubfieldJoiner() {

			@Override
			public List<String> extract(DataField df, String separator,
					char... subfields) {
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

	};

	public abstract SubfieldJoiner getJoiner();

}