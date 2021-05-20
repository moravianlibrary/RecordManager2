package cz.mzk.recordmanager.server.index.indexIntercepting;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.marc.marc4j.RecordImpl;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

public class DefaultMarcIndexInterceptor implements IndexInterceptor {

	private final MarcRecord marcRecord;

	public DefaultMarcIndexInterceptor(MarcRecord marcRecord) {
		this.marcRecord = marcRecord;
	}

	@Override
	public MarcRecord intercept() {
		Record newRecord = new RecordImpl();
		newRecord.setLeader(marcRecord.getLeader());
		for (ControlField cf : marcRecord.getRecord().getControlFields()) {
			newRecord.addVariableField(cf);
		}

		for (DataField df : marcRecord.getRecord().getDataFields()) {
			// remove fields 996 with q0
			if (df.getTag().equals("996") && df.getSubfield('q') != null && df.getSubfield('q').getData().equals("0")) {
				continue;
			}
			newRecord.addVariableField(df);
		}

		return new MarcRecordImpl(newRecord);
	}


}
