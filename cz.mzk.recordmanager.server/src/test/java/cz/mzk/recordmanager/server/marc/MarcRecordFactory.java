package cz.mzk.recordmanager.server.marc;

import cz.mzk.recordmanager.server.marc.marc4j.MarcFactoryImpl;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarcRecordFactory {
	
	/* 
	 * Leader:
	 * 	 3 character tag 000
	 * 	 space
	 * 	 24 character tag's value
	 *   example: "000 00759cam a2200229 a 4500"
	 *
	 * ControlField:
	 *   3 character tag 00[1-9]
	 *   space
	 *   tag's value
	 *   example: "001 001445152"
	 * 
	 * DataField:
	 *   3 character tag - all digits
	 *   space
	 *   indicator1
	 *   indicator2
	 *   tag's value, perhaps with internal Subfields: 1 character $
	 *                                                 1 character tag
	 *                                                 value
	 *                                                 example: "$aCambridge" 
	 *   example: "991 00$b201503$cNOV$gg$en"
	 *   
	 *   or
	 *   
	 *   3 character tag - all digits
	 *   space
	 *   tag's value, perhaps with internal Subfields
	 *   example - indicators set on ' ': "991 $b201503$cNOV$gg$en"
	 */

	private static final Pattern LEADER = Pattern.compile("0{3}");
	private static final Pattern CONTROLFIELD = Pattern.compile("00[1-9]");
	private static final Pattern DATAFIELD = Pattern.compile("\\w{3}");
	private static final Pattern SUBFIELD = Pattern.compile("\\$([a-zA-Z0-9])([^$]*)");

	public static MarcRecordImpl recordFactory(List<String> data) throws Exception {
		MarcFactory marcFactory = MarcFactoryImpl.newInstance();
		Record record = marcFactory.newRecord();

		for (String field : data) {
			String key;
			String value;

			if (field.length() >= 3) {
				key = field.substring(0, 3);
				value = (field.length() > 3) ? field.substring(4, field.length()) : "";
			} else continue;

			// Leader
			if (LEADER.matcher(key).matches()) {
				if (value.length() > 24) value = value.substring(0, 24);
				else value = String.format("%-24s", value);
				record.setLeader(marcFactory.newLeader(value));

			}
			// ControlField
			else if (CONTROLFIELD.matcher(key).matches())
				record.addVariableField(marcFactory.newControlField(key, value));
				// DataField
			else if (DATAFIELD.matcher(key).matches()) {
				if (value.isEmpty()) record.addVariableField(marcFactory.newDataField(key, ' ', ' '));
				else if (value.length() >= 2) {
					// Indicators
					DataField dataField;
					if (value.charAt(0) == '$') dataField = marcFactory.newDataField(key, ' ', ' ');
					else if (value.charAt(1) == '$') dataField = marcFactory.newDataField(key, value.charAt(0), ' ');
					else dataField = marcFactory.newDataField(key, value.charAt(0), value.charAt(1));

					// Subfield
					if (value.length() > 2) {
						Matcher matcher = SUBFIELD.matcher(value);
						while (matcher.find()) {
							dataField.addSubfield(marcFactory.newSubfield(matcher.group(1).charAt(0), matcher.group(2)));
						}
					}
					record.addVariableField(dataField);
				}
			}
		}

		return new MarcRecordImpl(record);
	}
}
