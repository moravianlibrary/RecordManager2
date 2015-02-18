package cz.mzk.recordmanager.server.scripting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.mzk.recordmanager.server.marc.MarcRecord;

public class MarcDSL {
	
	private final MarcRecord record;
	
	private final Pattern FIELD_PATTERN = Pattern.compile("([0-9]{3})([a-zA-Z0-9]*)");

	public MarcDSL(MarcRecord record) {
		super();
		this.record = record;
	}

	public String getField(String tag) {
		Matcher matcher = FIELD_PATTERN.matcher(tag);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("Tag can't be parsed: "+ tag);
		}
		String fieldTag = matcher.group(1);
		String subFields = matcher.group(2);
		return record.getField(fieldTag, subFields.toCharArray());
	}
	
	public String getFormat(String arg) {
		return record.getFormat();
	}

}
