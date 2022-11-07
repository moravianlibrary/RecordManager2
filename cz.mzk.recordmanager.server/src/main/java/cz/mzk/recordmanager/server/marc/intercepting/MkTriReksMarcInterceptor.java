package cz.mzk.recordmanager.server.marc.intercepting;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


public class MkTriReksMarcInterceptor extends DefaultMarcInterceptor {

	public MkTriReksMarcInterceptor(Record record, ImportConfiguration configuration, String recordId) {
		super(record, configuration, recordId);
	}

	@Override
	public byte[] intercept() {
		if (super.getRecord() == null) {
			return new byte[0];
		}

		for (DataField df : new ArrayList<>(getRecord().getDataFields())) {
			if (!df.getTag().equals("996")) continue;
			if (df.getTag().equals("996")
					&& df.getSubfield('q') != null
					&& df.getSubfield('q').getData().equals("0")) {
				getRecord().removeVariableField(df);
			}
		}

		return new MarcRecordImpl(getRecord()).export(IOFormat.XML_MARC).getBytes(StandardCharsets.UTF_8);
	}

}
