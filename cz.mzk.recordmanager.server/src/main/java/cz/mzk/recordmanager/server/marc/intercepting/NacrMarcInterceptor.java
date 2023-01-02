package cz.mzk.recordmanager.server.marc.intercepting;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.util.RecordUtils;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

import java.nio.charset.StandardCharsets;

public class NacrMarcInterceptor extends DefaultMarcInterceptor {

	public NacrMarcInterceptor(Record record, ImportConfiguration configuration, String recordId) {
		super(record, configuration, recordId);
	}

	@Override
	public byte[] intercept() {
		if (super.getRecord() == null) {
			return new byte[0];
		}

		for (VariableField vf : super.getRecord().getVariableFields("996")) {
			DataField df = (DataField) vf;
			Subfield sf = df.getSubfield('s');
			if (sf == null) continue;
			if (sf.getData().equals("A")) sf.setData("P");
		}

		return new MarcRecordImpl(RecordUtils.sortFields(super.getRecord())).export(IOFormat.XML_MARC).getBytes(StandardCharsets.UTF_8);
	}

}
