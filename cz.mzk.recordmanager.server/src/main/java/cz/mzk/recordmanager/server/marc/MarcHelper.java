package cz.mzk.recordmanager.server.marc;

import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

public class MarcHelper {

	public static String getField(Record record, String tag, char subfield) {
		VariableField field = record.getVariableField(tag);
		if (field instanceof DataField) {
			DataField df = (DataField) field;
			Subfield sf = df.getSubfield(subfield);
			if (sf != null) {
				return sf.getData();
			}
		}
		return null;
	}
	
}
