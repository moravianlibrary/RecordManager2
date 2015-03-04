package cz.mzk.recordmanager.server.scripting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.mzk.recordmanager.server.marc.MarcRecord;

public class MarcDSL {

	private final static String EMPTY_SEPARATOR = "";

	private final MarcRecord record;

	private final MappingResolver propertyResolver;

	private final Pattern FIELD_PATTERN = Pattern
			.compile("([0-9]{3})([a-zA-Z0-9]*)");

	public MarcDSL(MarcRecord record, MappingResolver propertyResolver) {
		super();
		this.record = record;
		this.propertyResolver = propertyResolver;
	}

	public String getField(String tag) {
		Matcher matcher = FIELD_PATTERN.matcher(tag);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("Tag can't be parsed: " + tag);
		}
		String fieldTag = matcher.group(1);
		String subFields = matcher.group(2);
		return record.getField(fieldTag, subFields.toCharArray());
	}

	public List<String> getFields(String tags) {
		List<String> result = new ArrayList<String>(0);
		for (String tag : tags.split(":")) {
			Matcher matcher = FIELD_PATTERN.matcher(tag);
			if (!matcher.matches()) {
				throw new IllegalArgumentException("Tag can't be parsed: "
						+ tag);
			}
			String fieldTag = matcher.group(1);
			String subFields = matcher.group(2);
			result.addAll(record.getFields(fieldTag, " ",
					subFields.toCharArray()));
		}
		return result;
	}

	public List<String> getLanguages() {
		List<String> languages = new ArrayList<String>();
		String f008 = record.getControlField("008");
		if (f008.length() > 38) {
			languages.add(f008.substring(35, 38));
		}
		languages.addAll(record.getFields("041", EMPTY_SEPARATOR, 'a'));
		languages.addAll(record.getFields("041", EMPTY_SEPARATOR, 'd'));
		languages.addAll(record.getFields("041", EMPTY_SEPARATOR, 'e'));
		return languages;
	}

	public String translate(String file, String input, String defaultValue)
			throws IOException {
		Mapping mapping = propertyResolver.resolve(file);
		String result = (String) mapping.get(input);
		if (result == null) {
			result = defaultValue;
		}
		return result;
	}

	public List<String> translate(String file, List<String> inputs,
			String defaultValue) throws IOException {
		List<String> translated = new ArrayList<String>();
		Mapping mapping = propertyResolver.resolve(file);
		for (String input : inputs) {
			String result = (String) mapping.get(input);
			if (result == null) {
				result = defaultValue;
			}
			if (result != null) {
				translated.add(result);
			}
		}
		return translated;
	}

	public String getFormat(String arg) {
		return record.getFormat();
	}

}
