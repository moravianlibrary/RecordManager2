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

	public BmcMarcInterceptor(Record record) {
		super(record);
	}

	private static final String CZMESH = "czmesh";

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
				switch (df.getTag()) {
				case "650":
				case "651":
				case "655":
					// add subfield $2 with value czmesh if ind2 = '2' and there is no $2mesh
					if (df.getIndicator2() == '2' && df.getSubfields('2').stream().noneMatch(s -> s.getData().contains(CZMESH))) {
						df.addSubfield(MARC_FACTORY.newSubfield('2', CZMESH));
					}
					newRecord.addVariableField(df);
					break;
				// remove field 990, 991
				case "990":
				case "991":
					break;
				default:
					newRecord.addVariableField(df);
				}
			}
		}
		return new MarcRecordImpl(newRecord).export(IOFormat.XML_MARC).getBytes(StandardCharsets.UTF_8);
	}
}
