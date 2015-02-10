package cz.mzk.recordmanager.server.marc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

import cz.mzk.recordmanager.server.util.MetadataUtils;

public class MarcRecordImpl implements MarcRecord {

	protected final Record record;
	
	protected final Map<String, List<DataField>> dataFields;
	protected final List<ControlField> controlFields;
	
	public MarcRecordImpl(Record record) {
		super();
		this.record = record;
		this.controlFields = record.getControlFields();
		this.dataFields = new HashMap<String, List<DataField>>();
		for (DataField field: record.getDataFields()) {
			List<DataField> list;
			if (dataFields.containsKey(field.getTag())) {
				list = dataFields.get(field.getTag());
			} else {
				list = new ArrayList<DataField>();
			}
			list.add(field);
			dataFields.put(field.getTag(), list);
		}
	}

	@Override
	public String getField(String tag, char subfield) {
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

	/**
	 * @return 245a:245b.245n.245p
	 */
	@Override
	public String getTitle() {
		final char titleSubfields[] = new char[]{'a','b','n','p'};
		final char punctiation[] = new char[]{':', '.', '.' };
		final DataField field = getDataField("245");
		final List<Subfield> subfields = getSubfields(field, titleSubfields);

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < titleSubfields.length; i++) {
			if (i >= subfields.size() || subfields.get(i).getCode() != titleSubfields[i]) {
				return builder.toString();
			}
			if (i > 0) {
				if (MetadataUtils.hasTrailingPunctuation(builder.toString())) {
					builder.append(" ");
				} else {
					builder.append(punctiation[i-1]);
				}
			}
			builder.append(subfields.get(i).getData());
		}
		return builder.toString();
	}
	
	/**
	 * get first code subfield from first field having given tag
	 * @param tag
	 * @param code
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
	 * @param tag
	 * @return {@link DataField} or null
	 */
	protected DataField getDataField(String tag) {
		List<DataField> fieldList = dataFields.get(tag);
		return fieldList.isEmpty() ? null : fieldList.get(0);
	}
	
	/**
	 * get all subfields of {@link DataField} with corresponding codes
	 * @param field
	 * @param codes
	 * @return {@link List} of matching {@link Subfield} objects
	 */
	protected List<Subfield> getSubfields(DataField field, char[] codes) {
		List<Subfield> sfList = new ArrayList<Subfield>();
		if (field == null) {
			return sfList;
		}
		
		for (Character c: codes) {
			for (Subfield subF: field.getSubfields(c)) {
				sfList.add(subF);
			}
		}
		return sfList;
	}
	
	protected boolean isControlTag(final String tag) {
		return tag.startsWith("00");
	}
}
