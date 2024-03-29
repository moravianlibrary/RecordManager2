package cz.mzk.recordmanager.server.marc.intercepting;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.marc.marc4j.RecordImpl;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeSet;


public class KfbzMarcInterceptor extends DefaultMarcInterceptor {

	public KfbzMarcInterceptor(Record record, ImportConfiguration configuration, String recordId) {
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
				if (df.getTag().equals("996")) {
					if (df.getSubfield('d') == null && (df.getSubfield('y') != null
							&& df.getSubfield('v') != null
							&& df.getSubfield('i') != null)) {
						StringJoiner sfD = new StringJoiner(" / ");
						sfD.add(df.getSubfield('y').getData());
						sfD.add(df.getSubfield('v').getData());
						sfD.add(df.getSubfield('i').getData());
						df.addSubfield(MARC_FACTORY.newSubfield('d', sfD.toString()));
					}
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
