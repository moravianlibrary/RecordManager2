package cz.mzk.recordmanager.server.scripting.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.marc.MarcRecord;

@Component
public class GetStatusesMarcFunctions implements MarcRecordFunctions {

	protected final static String ABSENT = "a";

	protected final static String PRESENT = "p";

	public List<String> getStatuses(MarcRecord record) {
		return getStatuses(record, "996");
	}

	private List<String> getStatuses(MarcRecord record, String statusField) {
		List<DataField> fields = record.getAllFields().get(statusField);
		if (fields == null || fields.isEmpty()) {
			return Collections.emptyList();
		}
		List<String> statuses = new ArrayList<String>();
		boolean present = false;
		boolean absent = false;
		boolean freeStack = false;
		for (DataField field : fields) {
			Subfield s = field.getSubfield('s');
			if (s == null) {
				continue;
			}
			String status = s.getData().toLowerCase();
			switch (status) {
			case ABSENT:
				absent = true;
				break;
			case PRESENT:
				present = true;
				break;
			}
			Subfield a = field.getSubfield('a');
			if (a != null && "0".equals(a.getData())) {
				freeStack = true;
			}
		}
		if (absent) {
			statuses.add("absent");
		}
		if (present) {
			statuses.add("present");
		}
		if (freeStack && !statuses.isEmpty()) {
			statuses.add("free_stack");
		}
		return statuses;
	}

}
