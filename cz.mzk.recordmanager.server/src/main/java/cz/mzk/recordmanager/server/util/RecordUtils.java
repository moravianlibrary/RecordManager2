package cz.mzk.recordmanager.server.util;

import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.marc.marc4j.MarcFactoryImpl;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class RecordUtils {

	private static MarcFactory factory = MarcFactoryImpl.newInstance();

	public static Record sortFields(Record record) {
		Record newRecord = factory.newRecord();
		newRecord.setLeader(record.getLeader());
		for (ControlField cf : record.getControlFields()) {
			newRecord.addVariableField(cf);
		}
		MarcRecord marc = new MarcRecordImpl(record);
		Map<String, List<DataField>> dfMap = marc.getAllFields();
		for (String tag : new TreeSet<>(dfMap.keySet())) { // sorted tags
			for (DataField df : dfMap.get(tag)) {
				newRecord.addVariableField(df);
			}
		}

		return newRecord;
	}

}
