package cz.mzk.recordmanager.server.marc.intercepting;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.marc.marc4j.RecordImpl;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

import java.nio.charset.StandardCharsets;

public class KkkvMarcInterceptor extends DefaultMarcInterceptor {

	private static final String TAG_DEL = "DEL";
	private static final String TAG_STA = "STA";
	private static final String TAG_914 = "914";
	private static final String CPK0 = "cpk0";

	public KkkvMarcInterceptor(Record record, ImportConfiguration configuration, String recordId) {
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
			// add 914 $acpk0 if exists field DEL or STA
			if (df.getTag().equals(TAG_DEL) || df.getTag().equals(TAG_STA)) {
				newRecord.addVariableField(MARC_FACTORY.newDataField(TAG_914, df.getIndicator1(), df.getIndicator2(), "a", CPK0));
			}
			// default
			processField996(df);
			newRecord.addVariableField(df);
		}
		return new MarcRecordImpl(newRecord).export(IOFormat.XML_MARC).getBytes(StandardCharsets.UTF_8);
	}

}
