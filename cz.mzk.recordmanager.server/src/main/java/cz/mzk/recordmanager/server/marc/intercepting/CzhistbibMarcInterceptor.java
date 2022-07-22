package cz.mzk.recordmanager.server.marc.intercepting;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.marc.marc4j.RecordImpl;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.util.RecordUtils;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

import java.nio.charset.StandardCharsets;

public class CzhistbibMarcInterceptor extends DefaultMarcInterceptor {

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
			// change field tag 691 to 653
			if (df.getTag().equals("691")) {
				df.setTag("653");
			}
			newRecord.addVariableField(df);
		}

		return new MarcRecordImpl(RecordUtils.sortFields(newRecord)).export(IOFormat.XML_MARC).getBytes(StandardCharsets.UTF_8);
	}

}
