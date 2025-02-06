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
import java.util.stream.Collectors;

public class RecordUtils {

	private static MarcFactory factory = MarcFactoryImpl.newInstance();

	public static Record sortFields(Record record) {
		Record newRecord = factory.newRecord();
		newRecord.setLeader(record.getLeader());
		TreeSet<String> controlTags = record.getControlFields().stream().map(ControlField::getTag).collect(Collectors.toCollection(TreeSet::new));
		for (String tag : controlTags) {
			newRecord.addVariableField(record.getVariableField(tag));
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
