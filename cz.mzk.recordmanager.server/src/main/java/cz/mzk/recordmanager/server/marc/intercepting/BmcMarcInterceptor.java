package cz.mzk.recordmanager.server.marc.intercepting;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.marc.marc4j.RecordImpl;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class BmcMarcInterceptor extends DefaultMarcInterceptor {

	private static final String LINK_STR = "https://www.medvik.cz/bmc/link.do?id=%s";
	private static final String LINK_SF_Y = "záznam v BMČ";

	public BmcMarcInterceptor(Record record) {
		super(record);
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
				// remove field 990, 991
				if (!df.getTag().equals("990") && !df.getTag().equals("991")) newRecord.addVariableField(df);
			}
		}
		// add 856 as link + 001
		if (newRecord.getControlNumber() != null) {
			DataField linkDf = MARC_FACTORY.newDataField("856", '4', '0',
					"u", String.format(LINK_STR, newRecord.getControlNumber()),
					"y", LINK_SF_Y);
			newRecord.addVariableField(linkDf);
		}
		return new MarcRecordImpl(newRecord).export(IOFormat.XML_MARC).getBytes(StandardCharsets.UTF_8);
	}
}
