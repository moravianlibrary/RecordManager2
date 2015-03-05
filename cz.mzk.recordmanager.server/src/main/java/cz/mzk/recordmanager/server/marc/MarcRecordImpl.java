package cz.mzk.recordmanager.server.marc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

import cz.mzk.recordmanager.server.util.MetadataUtils;

public class MarcRecordImpl implements MarcRecord {

	private static final String DEFAULT_SEPARATOR = " ";
	
	private static final String EMPTY_STRING = "";
	
	protected final Record record;
	
	protected final Map<String, List<DataField>> dataFields;
	
	protected final Map<String, List<ControlField>> controlFields;
	
	public MarcRecordImpl(Record record) {
		super();
		this.record = record;
		this.controlFields = new HashMap<String, List<ControlField>>();
		for (ControlField field: record.getControlFields()) {
			List<ControlField> list;
			if (controlFields.containsKey(field.getTag())) {
				list = controlFields.get(field.getTag());
			} else {
				list = new ArrayList<ControlField>();
			}
			list.add(field);
			controlFields.put(field.getTag(), list);
		}
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
			return parseSubfields((DataField) fields.get(0), separator, subfields);
		}
		return null;
	}
	
	public List<String> getFields(String tag, String separator, char... subfields) {
		return getFields(tag, MatchAllDataFieldMatcher.INSTANCE, separator, subfields);
	}
	
	@Override
	public List<String> getFields(String tag, DataFieldMatcher matcher, String separator,
			char... subfields) {
		List<DataField> fields = dataFields.get(tag);
		if (fields == null) {
			return Collections.emptyList();
		}
		List<String> result = new ArrayList<String>(fields.size());
		for (DataField field : fields) {
			if (matcher.matches(field)) {
				String content = parseSubfields(field ,separator, subfields);
				result.add(content);
			}
		}
		return result;
	}
	
	@Override
	public Map<String, List<DataField>> getAllFields() {
		return Collections.unmodifiableMap(dataFields);
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
	
	protected String parseSubfields(DataField df, String separator, char... subfields) {
		StringBuilder sb = new StringBuilder();
		String sep = EMPTY_STRING;
		for (char subfield : subfields) {
			Subfield sf = df.getSubfield(subfield);
			if (sf != null) {
				sb.append(sep);
				sb.append(sf.getData());
				sep = separator;
			}
		}
		return sb.toString();
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
	
	public String getFormat() {
		boolean onlineResource = false;
		List<ControlField> cfl = controlFields.containsKey("007") ? 
				controlFields.get("007") : new ArrayList<ControlField>();
				
		for (ControlField field : cfl) {
			String data = field.getData();
			if (data.length() < 2) {
				continue;
			}
			char code1 = Character.toUpperCase(data.charAt(0));
			char code2 = Character.toUpperCase(data.charAt(1));

			switch (code1) {
			case 'A':
				switch (code2) {
				case 'D':
					return "Atlas";
				default:
					return "Map";
				}
			case 'C':
				switch (code2) {
				case 'A':
					return "TapeCartridge";
				case 'B':
					return "ChipCartridge";
				case 'C':
					return "DiscCartridge";
				case 'F':
					return "TapeCassette";
				case 'H':
					return "TapeReel";
				case 'J':
					return "FloppyDisk";
				case 'M':
				case 'O':
					return "CDROM";
				case 'R':
					// Do not return - this will cause anything with an
					// 856 field to be labeled as "Electronic"
					onlineResource = true;
					break;
				default:
					return "Electronic";
				}
				break;
			case 'D':
				return "Globe";
			case 'F':
				return "Braille";
			case 'G':
				switch (code2) {
				case 'C':
				case 'D':
					return "Filmstrip";
				case 'T':
					return "Transparency";
				default:
					return "Slide";
				}
			case 'H':
				return "Microfilm";
			case 'K':
				switch (code2) {
				case 'C':
					return "Collage";
				case 'D':
					return "Drawing";
				case 'E':
					return "Painting";
				case 'F':
					return "Print";
				case 'G':
					return "Photonegative";
				case 'J':
					return "Print";
				case 'L':
					return "TechnicalDrawing";
				case 'O':
					return "FlashCard";
				case 'N':
					return "Chart";
				default:
					return "Photo";
				}
			case 'M':
				switch (code2) {
				case 'F':
					return "VideoCassette";
				case 'R':
					return "Filmstrip";
				default:
					return "MotionPicture";
				}
			case 'O':
				return "Kit";
			case 'Q':
				return "MusicalScore";
			case 'R':
				return "SensorImage";
			case 'S':
				switch (code2) {
				case 'D':
					if (data.length() > 14) {
						return Character.toUpperCase(data.charAt(13)) == 'D' ? "CD" : "SoundDisc";
					}
					break;
				case 'S':
					return "SoundCassette";
				default:
					return "SoundRecording";
				}
			case 'V':
				if (data.length() < 5) {
					break;
				}
				switch (Character.toUpperCase(data.charAt(4))) {
				case 'S':
					return "BluRay";
				case 'V':
					return "DVD";
				}
				switch (code2) {
				case 'C':
					return "VideoCartridge";
				case 'D':
					return "VideoDisc";
				case 'F':
					return "VideoCassette";
				case 'R':
					return "VideoReel";
				default:
					return "Video";
				}
			}
		}

		char leaderCode = record.getLeader().getTypeOfRecord();
		
		switch (Character.toUpperCase(leaderCode)) {
		case 'C':
		case 'D':
			return "MusicalScore";
		case 'E':
		case 'F':
			return "Map";
		case 'G':
			return "Slide";
		case 'I':
			return "SoundRecording";
		case 'J':
			return "MusicRecording";
		case 'K':
			return "Photo";
		case 'M':
			return "Electronic";
		case 'O':
		case 'P':
			return "Kit";
		case 'R':
			return "PhysicalObject";
		case 'T':
			return "Manuscript";
		}
		
		leaderCode = record.getLeader().getImplDefined1()[0];
		
		switch (Character.toUpperCase(leaderCode)) {
		// Monograph
		case 'M':
			if (onlineResource) {
				return "eBook";
			} else {
				return "Book";
			}
		// Serial
		case 'S':
			// Look in 008 to determine what type of Continuing Resource
			List<ControlField> innerCfl = controlFields.containsKey("008") ? 
					controlFields.get("008") : new ArrayList<ControlField>();
			for (ControlField innerField : innerCfl) {
				if (innerField.getData().length() < 23) {
					continue;
				}
				char innerCode = innerField.getData().charAt(22);
				switch (Character.toUpperCase(innerCode)) {
				case 'N':
					return onlineResource ? "eNewspaper" : "Newspaper";
				case 'P':
					return onlineResource ? "eJournal" : "Journal";
				default:
					return onlineResource ? "eSerial" : "Serial";
				}
			}
			break;
		case 'A':
			// Component part in monograph
			return onlineResource ? "eBookSection" : "BookSection";
		case 'B':
			// Component part in serial
			return onlineResource ? "eArticle" : "Article";
		case 'C':
			// Collection
			return "Collection";
		case 'D':
			// Component part in collection (sub unit)
			return "SubUnit";
		case 'I':
			// Integrating resource
			return "ContinuouslyUpdatedResource";
		}
		return "Other";
			
	}

	@Override
	public Long getPublicationYear() {
		String year = getField("260", 'c');
		try {
			return Long.parseLong(year);
		} catch (NumberFormatException e) {}
		return null;
		
	}
}
