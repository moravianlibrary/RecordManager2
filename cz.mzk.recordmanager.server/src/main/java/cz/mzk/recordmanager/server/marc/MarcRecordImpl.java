package cz.mzk.recordmanager.server.marc;

import cz.mzk.recordmanager.server.util.MarcRecordUtils;
import info.freelibrary.marc4j.impl.LeaderImpl;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.*;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.marc4j.MarcFactoryImpl;

public class MarcRecordImpl implements MarcRecord {

	private static final String DEFAULT_SEPARATOR = " ";

	protected final Record record;

	protected final Map<String, List<DataField>> dataFields;

	protected final Map<String, List<ControlField>> controlFields;

	public MarcRecordImpl(Record record) {
		super();
		this.record = record;
		this.controlFields = new HashMap<>();
		for (ControlField field : record.getControlFields()) {
			List<ControlField> list;
			if (controlFields.containsKey(field.getTag())) {
				list = controlFields.get(field.getTag());
			} else {
				list = new ArrayList<>();
			}
			list.add(field);
			controlFields.put(field.getTag(), list);
		}
		this.dataFields = new HashMap<>();
		for (DataField field : record.getDataFields()) {
			List<DataField> list;
			if (dataFields.containsKey(field.getTag())) {
				list = dataFields.get(field.getTag());
			} else {
				list = new ArrayList<>();
			}
			list.add(field);
			dataFields.put(field.getTag(), list);
		}
	}

	@Override
	public String getControlField(String tag) {
		List<ControlField> fields = controlFields.get(tag);
		if (fields == null) {
			return null;
		} else {
			return fields.get(0).getData();
		}
	}

	@Override
	public String getField(String tag, char... subfields) {
		return getField(tag, DEFAULT_SEPARATOR, subfields);
	}

	@Override
	public String getField(String tag, String separator, char... subfields) {
		List<DataField> fields = dataFields.get(tag);
		if (fields != null && fields.size() > 0) {
			String content = parseSubfields(fields.get(0), separator, subfields);
			if (content != null && !content.trim().isEmpty()) {
				return content;
			}
		}
		return null;
	}

	public List<String> getFields(String tag, String separator, char... subfields) {
		return getFields(tag, MatchAllDataFieldMatcher.INSTANCE, separator, subfields);
	}

	@Override
	public List<String> getFields(String tag, DataFieldMatcher matcher, String separator, char... subfields) {
		List<DataField> fields = dataFields.get(tag);
		if (fields == null) {
			return Collections.emptyList();
		}
		List<String> result = new ArrayList<>(fields.size());
		for (DataField field : fields) {
			if (matcher.matches(field)) {
				String content = parseSubfields(field, separator, subfields);
				if (content != null && !content.trim().isEmpty()) {
					result.add(content);
				}
			}
		}
		return result;
	}

	@Override
	public List<String> getFields(String tag, DataFieldMatcher matcher, SubfieldExtractionMethod method, String separator,
								  char... subfields) {
		if (matcher == null) {
			matcher = MatchAllDataFieldMatcher.INSTANCE;
		}
		List<DataField> fields = dataFields.get(tag);
		if (fields == null) {
			return Collections.emptyList();
		}
		List<String> result = new ArrayList<>(fields.size());
		for (DataField field : fields) {
			if (matcher.matches(field)) {
				List<String> sfValues = parseSubfields(field, separator, method, subfields);
				for (String sfValue : sfValues) {
					if (sfValue != null && !sfValue.trim().isEmpty()) {
						result.add(sfValue);
					}
				}
			}
		}
		return result;
	}

	@Override
	public Map<String, List<DataField>> getAllFields() {
		return Collections.unmodifiableMap(dataFields);
	}

	protected List<String> parseSubfields(DataField df, String separator, SubfieldExtractionMethod method, char... subfields) {
		if (method == null) {
			method = SubfieldExtractionMethod.JOINED;
		}
		return method.getJoiner().extract(df, separator, subfields);
	}

	protected String parseSubfields(DataField df, String separator, char... subfields) {
		return MarcRecordUtils.parseSubfields(df, separator, subfields);
	}

	/**
	 * get first code subfield from first field having given tag
	 *
	 * @param tag  {@link DataField} tag
	 * @param code {@link Subfield} code
	 * @return {@link Subfield} or null
	 */
	protected Subfield getSubfield(String tag, char code) {
		DataField field = getDataField(tag);
		if (field == null) {
			return null;
		}

		List<Subfield> subfields = getSubfields(field, new char[]{code});
		return subfields.isEmpty() ? null : subfields.get(0);
	}

	/**
	 * get first {@link DataField} with given tag
	 *
	 * @param tag {@link DataField} tag
	 * @return {@link DataField} or null
	 */
	protected DataField getDataField(String tag) {
		List<DataField> fieldList = dataFields.get(tag);
		return fieldList.isEmpty() ? null : fieldList.get(0);
	}

	/**
	 * get all subfields of {@link DataField} with corresponding codes
	 *
	 * @param field {@link DataField}
	 * @param codes {@link Subfield} codes
	 * @return {@link List} of matching {@link Subfield} objects
	 */
	public List<Subfield> getSubfields(DataField field, char[] codes) {
		List<Subfield> sfList = new ArrayList<>();
		if (field == null) {
			return sfList;
		}

		for (Character c : codes) {
			sfList.addAll(field.getSubfields(c));
		}
		return sfList;
	}

	protected boolean isControlTag(final String tag) {
		return tag.startsWith("00");
	}

	public String export(IOFormat iOFormat) {
		IOFormat usedFormat = iOFormat == null ? IOFormat.XML_MARC : iOFormat;
		switch (usedFormat) {
		case LINE_MARC:
			return exportToLineMarc();
		case ALEPH_MARC:
			return exportToAlephMarc();
		case ISO_2709:
			return exportToIso2709();
		default:
			return exportToXML();
		}
	}

	protected String exportToLineMarc() {
		return record.toString();
	}

	protected String exportToAlephMarc() {
		StringBuilder out = new StringBuilder();

		out.append(record.getControlNumber());
		out.append(" LDR   L ");
		out.append(record.getLeader());
		out.append('\n');

		for (ControlField field : record.getControlFields()) {
			out.append(record.getControlNumber());
			out.append(' ');
			out.append(field.getTag());
			out.append("   L ");
			out.append(field.getData());
			out.append('\n');
		}

		for (DataField field : record.getDataFields()) {
			out.append(record.getControlNumber());
			out.append(' ');
			out.append(field.getTag());
			out.append(field.getIndicator1());
			out.append(field.getIndicator2());
			out.append(" L ");
			for (Subfield sfield : field.getSubfields()) {
				out.append("$$");
				out.append(sfield.getCode());
				out.append(sfield.getData());
			}
			out.append('\n');
		}

		return out.toString();
	}

	protected String exportToIso2709() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		MarcWriter writer = new MarcStreamWriter(stream, "UTF-8", true);
		writer.write(record);
		writer.close();
		return new String(stream.toByteArray(), StandardCharsets.UTF_8);
	}

	protected String exportToXML() {
		OutputStream stream = new ByteArrayOutputStream();
		MarcXmlWriter writer = new MarcXmlWriter(stream, "UTF-8");
		writer.write(record);
		writer.close();
		return stream.toString();
	}

	@Override
	public List<DataField> getDataFields(String tag) {
		return dataFields.get(tag) == null ? Collections.emptyList() : dataFields.get(tag);
	}

	@Override
	public List<ControlField> getControlFields(String tag) {
		return controlFields.get(tag) == null ? Collections.emptyList() : controlFields.get(tag);
	}

	@Override
	public Leader getLeader() {
		return record.getLeader() == null ? new LeaderImpl() : record.getLeader();
	}

	@Override
	public void addDataField(String tag, char ind1, char ind2, String... subfields) {
		MarcFactory factory = MarcFactoryImpl.newInstance();
		DataField df = factory.newDataField(tag, ind1, ind2, subfields);
		record.addVariableField(df);
	}

	@Override
	public Record getRecord() {
		return record;
	}

}
