package cz.mzk.recordmanager.server.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.validator.routines.ISBNValidator;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;

import cz.mzk.recordmanager.server.export.IOFormat;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecordFormat;
import cz.mzk.recordmanager.server.util.MetadataUtils;

public class MetadataMarcRecord implements MetadataRecord {

	protected MarcRecord underlayingMarc;
	
	protected final ISBNValidator isbnValidator = ISBNValidator.getInstance(true);

	protected static final Pattern PAGECOUNT_PATTERN = Pattern.compile("\\d+");
	protected static final Pattern YEAR_PATTERN = Pattern.compile("\\d{4}");
	
	public MetadataMarcRecord(MarcRecord underlayingMarc) {
		if (underlayingMarc == null) {
			throw new IllegalArgumentException("Creating MetadataMarcRecord with NULL underlayingMarc.");
		}
		this.underlayingMarc = underlayingMarc;
	}
	
	@Override
	public String getUniqueId() {
		// TODO override this implementation in institution specific classes
		String id = underlayingMarc.getControlField("001");
		if (id == null) {
			id = underlayingMarc.getField("995", 'a');
		}
		return id;
	}

	@Override
	public List<String> getISSNs() {	
		List<String> issns = new ArrayList<String>();
		
		for(String issn: underlayingMarc.getFields("022", 'a')){
			issns.add(issn.replace("-", ""));
		}
		
		return issns;
		
	}

	@Override
	public String getSeriesISSN() {	
		return underlayingMarc.getField("490", 'x');
	}

	@Override
	public Long getPageCount() {		
		String count = underlayingMarc.getField("300", 'a');
		if(count == null){
			return null;
		}	
		
		Matcher matcher = PAGECOUNT_PATTERN.matcher(count);
		try {
			if (matcher.find()) {
				return Long.parseLong(matcher.group(0));
			}
		} catch (NumberFormatException e) {}
		return null;
	}
	
	@Override
	public List<String> getISBNs() {
		List<String> isbns = new ArrayList<String>();
		
		for(String isbn: underlayingMarc.getFields("020", 'a')){	
			isbn = isbnValidator.validate(isbn);
			if(isbn == null) continue;
			if(!isbns.contains(isbn)) isbns.add(isbn);
		}
		
		return isbns;
	}
	
	public String getFormat() {
		boolean onlineResource = false;
		List<ControlField> cfl = underlayingMarc.getControlFields("007");
				
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

		char leaderCode = underlayingMarc.getLeader().getTypeOfRecord();
		
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
		
		leaderCode = underlayingMarc.getLeader().getImplDefined1()[0];
		
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
			List<ControlField> innerCfl = underlayingMarc.getControlFields("008");		
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
		String year = underlayingMarc.getField("260", 'c');
		if (year == null) {
			return null;
		}
		Matcher matcher = YEAR_PATTERN.matcher(year);
		try {
			if (matcher.find()) {
				return Long.parseLong(matcher.group(0));
			}
		} catch (NumberFormatException e) {}
		return null;
	}
		
	/**
	 * @return 245a:245b.245n.245p
	 */
	@Override
	public String getTitle() {
		final char titleSubfields[] = new char[]{'a','b','n','p'};
		final char punctiation[] = new char[]{':', '.', '.' };
		final DataField field = underlayingMarc.getDataFields("245").get(0);
		final List<Subfield> subfields = underlayingMarc.getSubfields(field, titleSubfields);

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

	@Override
	public String export(IOFormat iOFormat) {
		return underlayingMarc.export(iOFormat);
	}

	@Override
	public List<HarvestedRecordFormat> getDetectedFormatList() {
		return Collections.emptyList();
	}
		
}
