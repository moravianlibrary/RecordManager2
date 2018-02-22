package cz.mzk.recordmanager.server.imports.classifier;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class PredictedRecord {

	private String recordId;
	private List<Pair<String, Float>> values = null;

	private static final String NAME_ID = "ID";
	private static final String NAME_BEST = "BEST_%d";
	private static final String NAME_BEST_VAL = "BEST_%d_VAL";
//	private static final String PSEUDO_072 = "$a%s$x%s$9%s";

//	private static final Pattern PATTERN_PREDICTED_VALUE = Pattern.compile("(\\d*) (.*)->([^ ]*) (.*)");

	private PredictedRecord(String recordId) {
		this.recordId = recordId;
	}

	public String getRecordId() {
		return recordId;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}

	public List<Pair<String, Float>> getValues() {
		return values;
	}

	public void setValues(List<Pair<String, Float>> values) {
		this.values = values;
	}

	private void addValue(String p072, String relevance) {
		if (values == null) values = new ArrayList<>();
		this.values.add(Pair.of(p072, Float.valueOf(relevance)));
	}

	public static PredictedRecord create(CSVRecord rawRecord) {
		PredictedRecord newRecord = new PredictedRecord(rawRecord.get(NAME_ID));
		int i = 1;
		while (rawRecord.isSet(String.format(NAME_BEST, i))) {
			newRecord.addValue(
					parsePredictedValue(rawRecord.get(String.format(NAME_BEST, i))),
					rawRecord.get(String.format(NAME_BEST_VAL, i))
			);
			++i;
		}
		return newRecord;
	}

	private static String parsePredictedValue(String raw) {
//		Matcher matcher;
//		if ((matcher = PATTERN_PREDICTED_VALUE.matcher(raw)).matches()) {
//			return String.format(PSEUDO_072, matcher.group(3), matcher.group(4), matcher.group(1));
//		}
		return raw;
	}

	@Override
	public String toString() {
		return "PredictedRecord{" +
				"recordId='" + recordId + '\'' +
				", values=" + values +
				'}';
	}
}
