package cz.mzk.recordmanager.server.marc.intercepting;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.marc.marc4j.RecordImpl;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.util.RecordUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CzhistbibMarcInterceptor extends DefaultMarcInterceptor {

	private static final List<Pair<String, String>> REPLACEMENT_PAIRS = new ArrayList<>();

	static {
		REPLACEMENT_PAIRS.add(Pair.of("668", "648"));
		REPLACEMENT_PAIRS.add(Pair.of("671", "651"));
		REPLACEMENT_PAIRS.add(Pair.of("673", "653"));
		REPLACEMENT_PAIRS.add(Pair.of("691", "653"));
	}

	public CzhistbibMarcInterceptor(Record record, ImportConfiguration configuration, String recordId) {
		super(record, configuration, recordId);
	}

	@Override
	public byte[] intercept() {
		if (super.getRecord() == null) {
			return new byte[0];
		}
		Record newRecord = new RecordImpl();
		newRecord.setLeader(getRecord().getLeader());
		for (ControlField cf : super.getRecord().getControlFields()) {
			newRecord.addVariableField(cf);
		}

		for (DataField df : super.getRecord().getDataFields()) {
			for (Pair<String, String> pair : REPLACEMENT_PAIRS) {
				if (df.getTag().equals(pair.getLeft())) {
					df.setTag(pair.getRight());
				}
			}
			newRecord.addVariableField(df);
		}

		return new MarcRecordImpl(RecordUtils.sortFields(newRecord)).export(IOFormat.XML_MARC).getBytes(StandardCharsets.UTF_8);
	}

}
