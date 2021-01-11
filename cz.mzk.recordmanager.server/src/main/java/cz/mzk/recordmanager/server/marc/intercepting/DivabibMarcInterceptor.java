package cz.mzk.recordmanager.server.marc.intercepting;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.marc.marc4j.MarcFactoryImpl;
import cz.mzk.recordmanager.server.util.RecordUtils;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

import java.nio.charset.StandardCharsets;

public class DivabibMarcInterceptor extends DefaultMarcInterceptor {

	private static final String TAG_072 = "072";

	public DivabibMarcInterceptor(Record record) {
		super(record);
	}

	@Override
	public byte[] intercept() {
		if (super.getRecord() == null) {
			return new byte[0];
		}

		MarcFactory marcFactory = new MarcFactoryImpl();

		if (getRecord().getVariableFields(TAG_072).isEmpty()) {
			getRecord().addVariableField(marcFactory.newDataField("072", ' ', '7', "a", "792",
					"x", "Divadlo. Divadelní představení", "2", "Konspekt", "9", "3"));
			setRecord(RecordUtils.sortFields(getRecord()));
		}
		return new MarcRecordImpl(getRecord()).export(IOFormat.XML_MARC).getBytes(StandardCharsets.UTF_8);
	}

}
