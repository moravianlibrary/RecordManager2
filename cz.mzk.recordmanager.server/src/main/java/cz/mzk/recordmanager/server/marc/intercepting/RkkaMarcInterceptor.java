package cz.mzk.recordmanager.server.marc.intercepting;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.marc.marc4j.RecordImpl;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.util.DawinciUtils;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class RkkaMarcInterceptor extends DefaultMarcInterceptor {

	private static final List<String> SFL = Arrays.asList("KM", "KO", "KA", "KD", "K2", "K1", "KN", "K6", "K4",
			"K7", "KI", "KU", "K8", "K9", "KF", "KC");

	public RkkaMarcInterceptor(Record record, ImportConfiguration configuration, String recordId) {
		super(record, configuration, recordId);
	}

	@Override
	public byte[] intercept() {
		if (super.getRecord() == null) {
			return new byte[0];
		}

		MarcRecord marc = new MarcRecordImpl(super.getRecord());
		Record newRecord = new RecordImpl();

		newRecord.setLeader(getRecord().getLeader());
		for (ControlField cf : super.getRecord().getControlFields()) {
			newRecord.addVariableField(cf);
		}

		Map<String, List<DataField>> dfMap = marc.getAllFields();
		for (String tag : new TreeSet<>(dfMap.keySet())) { // sorted tags
			for (DataField df : dfMap.get(tag)) {
				// remove fields 990, 991
				if (df.getTag().equals("990") || df.getTag().equals("991")) {
					continue;
				}
				if (df.getTag().equals("996")) {
					// ignore field 996 when $l not contains KM,KO,KA,KD,K2,K1,KN,K6,K4,K7,KI,KU,K8,K9,KF,KC
					Subfield sb = df.getSubfield('l');
					if (sb != null && !SFL.contains(sb.getData())) continue;
					df = DawinciUtils.createSubfieldD(df);
					processField996(df);
					newRecord.addVariableField(df);
				} else {
					newRecord.addVariableField(df);
				}
			}
		}

		return new MarcRecordImpl(newRecord).export(IOFormat.XML_MARC).getBytes(StandardCharsets.UTF_8);
	}

}
