package cz.mzk.recordmanager.server.scripting.marc.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.marc.MarcRecord;

@Component
public class GetStatusesMarcFunctions implements MarcRecordFunctions {

	protected final static String ABSENT = "a";

	protected final static String PRESENT = "p";
	
	protected final static String ONLINE = "online";
	protected final static Pattern ONLINE_PATTERN = Pattern.compile("(?i).*"+ONLINE+".*");
	
	public List<String> getStatuses(MarcRecord record) {
		List<String> statuses = new ArrayList<String>();
		statuses.addAll(getStatuses(record, "996"));
		statuses.add(getStatusFrom856(record, "856"));		
		return statuses;
	}

	private String getStatusFrom856(MarcRecord record, String statusField) {
		for(DataField field: record.getDataFields(statusField)){
			if(field.getIndicator1() == '4' && (field.getIndicator2() == '0' || field.getIndicator2() =='1')){			
				return ONLINE;
			}
		}
		return null;
	}

	private List<String> getStatuses(MarcRecord record, String statusField) {
		List<DataField> fields = record.getAllFields().get(statusField);
		if (fields == null || fields.isEmpty()) {
			return Collections.emptyList();
		}
		List<String> statuses = new ArrayList<String>();
		boolean present = false;
		boolean absent = false;
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
		}
		if (absent) {
			statuses.add("absent");
		}
		if (present) {
			statuses.add("present");
		}
		return statuses;
	}

}
